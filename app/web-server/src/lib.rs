use anyhow::Result;
use std::{time::Duration};
use tokio::time;
use tokio_stream::wrappers::IntervalStream;
use warp::{Filter, ws::Ws};
use futures_util::StreamExt; // Required to use `.split()` and `.next()`
use futures_util::SinkExt;
use serde_json::json;
use std::ffi::CString;
use std::sync::{Arc, Mutex}; // Function to handle WebSocket connections
use std::net::Ipv4Addr;

const WIDTH: u32 = 800;
const HEIGHT: u32 = 800;
const LENGTH: usize = (WIDTH * HEIGHT * 4) as usize;
const DIB_HEADER_SIZE: usize = 40;
const PIXELS_PER_METER: u32 = 2835;
const BITS_PER_PIXEL: u16 = 32;
const COMPRESSION: u32 = 0;
pub const BMP_HEADER_SIZE: usize = 54;
pub const PIXEL_BYTE_SIZE: usize = 4;
static EMPTY_BITMAP: &[u8] = &[0; BMP_HEADER_SIZE + WIDTH as usize * HEIGHT as usize * PIXEL_BYTE_SIZE]; // Set an empty bitmap with zeros

#[link(name = "cpp", kind = "dylib")]
extern "C" {
    fn start_rendering();
    fn get_bitmap() -> *const u8;
    fn select_obj_path(input: *const libc::c_char) -> ();
}

// Safe wrapper that calls MobileRT start rendering
pub fn start_rendering_safe() {
    unsafe { start_rendering() };
}

// Safe wrapper that returns a slice if the pointer is valid
pub fn get_bitmap_safe(len: usize) -> Option<&'static [u8]> {
    unsafe {
        let ptr = get_bitmap();
        if ptr.is_null() {
            None
        } else {
            Some(std::slice::from_raw_parts(ptr, len))
        }
    }
}

// Safe wrapper that selects OBJ file for MobileRT 
pub fn select_obj_path_safe(input: *const libc::c_char) -> () {
    unsafe { select_obj_path(input) };
}


// Handler for the button action
async fn restart_button_action_handler(body: serde_json::Value, should_update: Arc<Mutex<bool>>) -> Result<impl warp::Reply, warp::Rejection> {
    println!("Button clicked with data: {:?}", body);

    {
        let mut update_flag: std::sync::MutexGuard<'_, bool> = should_update.lock().unwrap();
        *update_flag = true; // Resume updates
    }

    start_rendering_safe();
    Ok(warp::reply::json(&json!({"status": "success"})))
}

async fn select_obj_button_action_handler(body: bytes::Bytes) -> Result<impl warp::Reply, warp::Rejection> {
    let input_string_result = String::from_utf8(body.to_vec()); // Returns Result<String, Utf8Error>
    let input_string: String = match input_string_result {
        Ok(value) => value, // Successfully decoded UTF-8 string
        Err(_) => {
            // Return a generic Warp rejection error
            return Err(warp::reject());
        }
    };

    println!("OBJ path: {:?}", input_string);
    let c_string: CString = CString::new(input_string).unwrap(); // Convert to CString
    select_obj_path_safe(c_string.into_raw());
    Ok(warp::reply::json(&json!({"status": "success"})))
}

pub async fn start_http_server() -> tokio::task::JoinHandle<()> {
    // Log the start of MobileRT
    println!("Initializing MobileRT...");
    // start_rendering_safe();

    // Set up HTTP server
    println!("Starting HTTP server asynchronously...");
    let server_address: ([u8; 4], u16) = ([127, 0, 0, 1], 3030);
    let server_task: tokio::task::JoinHandle<()> = tokio::spawn(async move {
        warp::serve(routes())
            .run(server_address)
            .await;
    });

    // Verify server startup (use a minimal delay or ensure health check implementation)
    println!(
        "HTTP server running at http://{}:{}/mobilert",
        Ipv4Addr::from(server_address.0),
        server_address.1
    );
    tokio::time::sleep(std::time::Duration::from_millis(10)).await;

    server_task
}

pub fn routes() -> impl Filter<Extract = impl warp::Reply, Error = warp::Rejection> + Clone {
    // Create the shared `Arc<Mutex<bool>>`
    let should_update: Arc<Mutex<bool>> = Arc::new(Mutex::new(true));

    // Clone `should_update` for each route that needs it
    let restart_should_update: Arc<Mutex<bool>> = should_update.clone();
    let ws_should_update: Arc<Mutex<bool>> = should_update.clone();

    // Route to serve the "index.html" file at /mobilert
    let mobilert_route = warp::path("mobilert")
        .and(warp::get())
        .and(warp::fs::file("static/index.html"));

    // Route to serve the entire "static" directory for other static files
    let static_files = warp::path("static").and(warp::fs::dir("static"));

    // Route for handling restart button action
    let restart_button_route = warp::path("api")
        .and(warp::path("restart"))
        .and(warp::post())
        .and(warp::body::json())
        .and(warp::any().map(move || restart_should_update.clone()))
        .and_then(restart_button_action_handler);

    // Route for handling selectObj button action
    let select_obj_button_route = warp::path("api")
        .and(warp::path("upload"))
        .and(warp::post())
        .and(warp::body::bytes())
        .and_then(select_obj_button_action_handler);

    // WebSocket route for transmitting bitmaps
    let ws_route = warp::path("ws")
        .and(warp::ws())
        .and(warp::any().map(move || ws_should_update.clone()))
        .map(|ws: Ws, should_update| ws.on_upgrade(move |websocket| async {
            println!("Client connected!");
            handle_websocket(websocket, should_update).await;
        }));

    // Combine the routes
    let routes = mobilert_route
        .or(static_files)
        .or(restart_button_route)
        .or(select_obj_button_route)
        .or(ws_route);

    return routes;
}

async fn handle_websocket(websocket: warp::ws::WebSocket, should_update: Arc<Mutex<bool>>) {
    let (mut transmitter, _receiver) = websocket.split();

    let interval: IntervalStream = IntervalStream::new(time::interval(Duration::from_millis(100)));
    tokio::pin!(interval);

    let mut last_bitmap: Option<Vec<u8>> = None;
    let mut unchanged_frames: u32 = 0;
    const MAX_UNCHANGED_FRAMES: u32 = 10;

    println!("Sending data from websocket");
    while let Some(_) = interval.next().await {
        let should_continue: bool = *should_update.lock().unwrap();
        if !should_continue {
            // println!("Pause updates since should_update flag is false");
            continue;
        }

        match generate_bitmap() {
            Ok(bitmap_data) => {
                // Check if the bitmap is all zeros (black image)
                let is_uniform_color = bitmap_data[BMP_HEADER_SIZE..] // Skip the first BMP_HEADER_SIZE bytes
                    .chunks(PIXEL_BYTE_SIZE) // Process the remaining data in chunks of PIXEL_BYTE_SIZE bytes (RGBA)
                    .all(|pixel| pixel == &bitmap_data[BMP_HEADER_SIZE..BMP_HEADER_SIZE+PIXEL_BYTE_SIZE]); // Compare each chunk with the first pixel (after BMP_HEADER_SIZE bytes)            

                if is_uniform_color {
                    println!("Bitmap is empty. Continuing updates.");
                    if let Some(last) = &last_bitmap {
                        // Check if the bitmap is unchanged
                        if *last == bitmap_data {
                            unchanged_frames += 1;
                            if unchanged_frames >= MAX_UNCHANGED_FRAMES {
                                println!("Bitmap unchanged for too long. Stopping updates.");
                                let mut update_flag = should_update.lock().unwrap();
                                *update_flag = false; // Stop updates
                                unchanged_frames = 0;
                            }
                            continue;
                        }
                    }
                } else {
                    println!("Bitmap updated.");
                    if let Some(last) = &last_bitmap {
                        // Check if the bitmap is unchanged
                        if *last == bitmap_data {
                            unchanged_frames += 1;
                            if unchanged_frames >= MAX_UNCHANGED_FRAMES {
                                println!("Bitmap unchanged for too long. Stopping updates.");
                                let mut update_flag = should_update.lock().unwrap();
                                *update_flag = false; // Stop updates
                                unchanged_frames = 0;
                            }
                            continue;
                        }
                    }
                }

                println!("Reset the unchanged counter and update the last bitmap");
                unchanged_frames = 0;
                last_bitmap = Some(bitmap_data.clone());

                if let Err(_e) = transmitter.send(warp::ws::Message::binary(bitmap_data)).await {
                    println!("Failed to send WebSocket message.");
                    break;
                }

            }
            Err(e) => {
                panic!("Error generating bitmap: {}", e);
            }
        }
    }

    println!("Closing connection websocket.");
    // Send a Close frame to the client
    let _ = transmitter.send(warp::ws::Message::close()).await;
    // After sending, the connection will close automatically
    println!("Closed connection websocket.");
}

// Generate binary data for the bitmap
fn generate_bitmap() -> Result<Vec<u8>, String> {
    let pixels_slice_option: Option<&[u8]> = get_bitmap_safe(LENGTH);
    let pixels_slice : &[u8] = match pixels_slice_option {
        Some(slice) => {
            println!("Bitmap retrieved from MobileRT.");
            slice
        }
        None => {
            println!("MobileRT didn't render anything. Returning an empty bitmap.");
            &EMPTY_BITMAP
        }
    };

    // Precompute file size and allocate vector
    let file_size: usize = LENGTH + BMP_HEADER_SIZE;
    let mut bmp_data: Vec<u8> = Vec::with_capacity(file_size);

    // Write BMP header
    bmp_data.extend_from_slice(&[
        0x42, 0x4D, // "BM"
        (file_size & 0xFF) as u8,
        ((file_size >> 8) & 0xFF) as u8,
        ((file_size >> 16) & 0xFF) as u8,
        ((file_size >> 24) & 0xFF) as u8, // File size
        0, 0, 0, 0,                      // Reserved
        BMP_HEADER_SIZE as u8, 0, 0, 0,  // Data offset
        DIB_HEADER_SIZE as u8, 0, 0, 0,  // DIB Header size
        (WIDTH & 0xFF) as u8,
        ((WIDTH >> 8) & 0xFF) as u8,
        ((WIDTH >> 16) & 0xFF) as u8,
        ((WIDTH >> 24) & 0xFF) as u8,    // Width
        (HEIGHT & 0xFF) as u8,
        ((HEIGHT >> 8) & 0xFF) as u8,
        ((HEIGHT >> 16) & 0xFF) as u8,
        ((HEIGHT >> 24) & 0xFF) as u8,   // Height
        1, 0,                            // Planes
        (BITS_PER_PIXEL & 0xFF) as u8, (BITS_PER_PIXEL >> 8) as u8, // Bit depth
        (COMPRESSION & 0xFF) as u8,
        ((COMPRESSION >> 8) & 0xFF) as u8,
        ((COMPRESSION >> 16) & 0xFF) as u8,
        ((COMPRESSION >> 24) & 0xFF) as u8, // Compression
        (LENGTH & 0xFF) as u8,
        ((LENGTH >> 8) & 0xFF) as u8,
        ((LENGTH >> 16) & 0xFF) as u8,
        ((LENGTH >> 24) & 0xFF) as u8,   // Image size
        (PIXELS_PER_METER & 0xFF) as u8,
        ((PIXELS_PER_METER >> 8) & 0xFF) as u8,
        0, 0,                            // Horizontal resolution
        (PIXELS_PER_METER & 0xFF) as u8,
        ((PIXELS_PER_METER >> 8) & 0xFF) as u8,
        0, 0,                            // Vertical resolution
        0, 0, 0, 0,                      // Colors in palette
        0, 0, 0, 0                       // Important colors
    ]);

    // Write pixel data (flipping vertically, rearranging RGBA to BGRA)
    let row_stride: usize = WIDTH as usize * PIXEL_BYTE_SIZE;
    bmp_data.extend(
        (0..HEIGHT).rev().flat_map(|row| {
            let start: usize = row as usize * row_stride;
            pixels_slice[start..start + row_stride]
                .chunks(PIXEL_BYTE_SIZE)
                .flat_map(|chunk: &[u8]| [chunk[2], chunk[1], chunk[0], chunk[3]]) // Rearrange to BGRA
        })
    );

    Ok(bmp_data)
}
