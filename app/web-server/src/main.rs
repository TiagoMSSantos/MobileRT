use anyhow::Result;
use base64::encode;
use std::slice;
use tokio;
use warp::Filter;
use serde_json::json;
use libc;
use std::ffi::CString;

// Define a custom error type
#[derive(Debug)]
struct CustomError {
    message: String,
}
impl warp::reject::Reject for CustomError {}

// Handler for the button action
async fn restart_button_action_handler(body: serde_json::Value) -> Result<impl warp::Reply, warp::Rejection> {
    println!("Button clicked with data: {:?}", body);
    unsafe { start_rendering() };
    Ok(warp::reply::json(&json!({"status": "success"})))
}

async fn select_obj_button_action_handler(body: bytes::Bytes) -> Result<impl warp::Reply, warp::Rejection> {
    let input_string_result = String::from_utf8(body.to_vec()); // Returns Result<String, Utf8Error>
    let input_string = match input_string_result {
        Ok(value) => value, // Successfully decoded UTF-8 string
        Err(_) => {
            // Handle the error case (rejection or alternative)
            return Err(warp::reject::custom(CustomError {
                message: "Invalid UTF-8".to_string(),
            }));
        }
    };

    println!("OBJ path: {:?}", input_string);
    let c_string = CString::new(input_string).unwrap(); // Convert to CString
    unsafe { select_obj_path(c_string.into_raw()) };
    Ok(warp::reply::json(&json!({"status": "success"})))
}

#[link(name = "cpp", kind = "dylib")]
extern "C" {
    fn start_rendering();
    fn get_bitmap() -> *const u8;
    fn select_obj_path(input: *const libc::c_char) -> ();
}

const WIDTH: u32 = 800;
const HEIGHT: u32 = 800;
const LENGTH: usize = (WIDTH * HEIGHT * 4) as usize;

#[tokio::main]
async fn main() -> Result<()> {
    println!("Hello, world!");

    // Start MobileRT
    unsafe { start_rendering() };

    // Start HTTP server
    start_http_server().await?;

    Ok(())
}

fn generate_bitmap_base64() -> Result<String> {
    let pixels = unsafe { get_bitmap() };

    // Convert the raw pointer to a slice
    let pixels_slice = unsafe { slice::from_raw_parts(pixels, LENGTH) };

    // Prepare the BMP header for an 800x800 image
    let file_size = LENGTH + 54;
    let mut bmp_data = Vec::with_capacity(file_size);
    bmp_data.extend_from_slice(&[
        0x42, 0x4D, // Signature "BM"
        (file_size & 0xFF) as u8, ((file_size >> 8) & 0xFF) as u8, ((file_size >> 16) & 0xFF) as u8, ((file_size >> 24) & 0xFF) as u8, // File size
        0, 0, 0, 0, // Reserved
        54, 0, 0, 0, // Data offset
        40, 0, 0, 0, // Header size
        (WIDTH & 0xFF) as u8, ((WIDTH >> 8) & 0xFF) as u8, ((WIDTH >> 16) & 0xFF) as u8, ((WIDTH >> 24) & 0xFF) as u8, // Width
        (HEIGHT & 0xFF) as u8, ((HEIGHT >> 8) & 0xFF) as u8, ((HEIGHT >> 16) & 0xFF) as u8, ((HEIGHT >> 24) & 0xFF) as u8, // Height
        1, 0, 32, 0, // Planes and bit count
        0, 0, 0, 0, // Compression
        (LENGTH & 0xFF) as u8, ((LENGTH >> 8) & 0xFF) as u8, ((LENGTH >> 16) & 0xFF) as u8, ((LENGTH >> 24) & 0xFF) as u8, // Image size
        0x13, 0x0B, 0, 0, 0x13, 0x0B, 0, 0, // Resolution
        0, 0, 0, 0, 0, 0, 0, 0 // Colors and important colors
    ]);

    // Flip the image vertically and swap red and blue channels in one pass
    let width_bytes = WIDTH as usize * 4;
    for row in (0..HEIGHT).rev() {
        let src_index = row as usize * width_bytes;
        for col in (0..WIDTH).map(|c| c as usize * 4) {
            bmp_data.extend_from_slice(&[
                pixels_slice[src_index + col + 2], // Blue (originally Red)
                pixels_slice[src_index + col + 1], // Green
                pixels_slice[src_index + col],     // Red (originally Blue)
                pixels_slice[src_index + col + 3], // Alpha
            ]);
        }
    }

    // Convert the BMP data to a base64 string
    let base64_bitmap = encode(&bmp_data);

    Ok(base64_bitmap)
}

async fn serve_bitmap() -> Result<impl warp::Reply, warp::Rejection> {
    match generate_bitmap_base64() {
        Ok(bitmap_base64) => Ok(warp::reply::html(bitmap_base64)),
        Err(e) => Ok(warp::reply::html(e.to_string())),
    }
}

async fn start_http_server() -> Result<()> {
    println!("started http_server");

    // Route to serve the "index.html" file at /mobilert
    let mobilert_route = warp::path("mobilert")
        .and(warp::get())
        .and(warp::fs::file("static/index.html"));

    // Route to serve the entire "static" directory for other static files
    let static_files = warp::path("static").and(warp::fs::dir("static"));

    // Route to handle /mobilert_bitmap
    let bitmap_route = warp::path("mobilert_bitmap")
        .and(warp::get())
        .and_then(serve_bitmap);

    // Route for handling restart button action
    let restart_button_route = warp::path("api")
        .and(warp::path("restart"))
        .and(warp::post())
        .and(warp::body::json()) // Parses JSON body
        .and_then(restart_button_action_handler);

    // Route for handling selectObj button action
    let select_obj_button_route = warp::path("api")
        .and(warp::path("upload"))
        .and(warp::post())
        .and(warp::body::bytes()) // Parses JSON body
        .and_then(select_obj_button_action_handler);

    // Combine the routes
    let routes = mobilert_route
        .or(static_files)
        .or(bitmap_route)
        .or(restart_button_route)
        .or(select_obj_button_route);

    // Start the Warp server
    println!("Server running at http://127.0.0.1:3030/mobilert");
    warp::serve(routes)
        .run(([127, 0, 0, 1], 3030))
        .await;

    println!("finished http_server");
    
    Ok(())
}
