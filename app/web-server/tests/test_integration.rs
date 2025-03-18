use web_server::start_http_server;
use web_server::routes;
use web_server::BMP_HEADER_SIZE;
use web_server::PIXEL_BYTE_SIZE;
use tokio::time::{timeout, Duration};
use warp::test::request;
use tokio_tungstenite::connect_async;
use futures::StreamExt; // For `.next()` on WebSocket stream

#[tokio::test]
async fn test_server() {
    // Start the HTTP server asynchronously
    let server_handle: tokio::task::JoinHandle<()> = start_http_server().await;
    assert!(!server_handle.is_finished(), "Server should be running");

    // Step 1: Test the POST `/api/restart` endpoint
    let response = request()
        .method("POST")
        .path("/api/restart")
        .body("{}")
        .reply(&routes()) // Assuming `routes()` configures the HTTP routes
        .await;

    assert_eq!(
        response.status(),
        200,
        "Expected a 200 OK response, but received {}",
        response.status()
    );

    // Define the WebSocket address as a constant for reuse
    const WS_ADDR: &str = "ws://127.0.0.1:3030/ws";

    // Step 2: Connect to WebSocket server
    let (mut socket, _response) = connect_async(WS_ADDR)
        .await
        .expect("Failed to connect to WebSocket");

    let mut is_bitmap_not_empty: bool = false;

    // Step 3: Validate WebSocket message
    while let Ok(Some(result)) = timeout(Duration::from_millis(500), socket.next()).await {
        let message: tokio_tungstenite::tungstenite::Message = result.unwrap();
        // Assert the WebSocket message type
        assert!(
            message.is_binary(),
            "Expected a binary WebSocket message, but got {:?}",
            message.is_binary()
        );

        // Extract and log the binary message
        let message_data: bytes::Bytes = message.into_data(); // Extract the binary data as Vec<u8>
        is_bitmap_not_empty = is_bitmap_not_empty || !message_data[BMP_HEADER_SIZE..] // Skip the first BMP_HEADER_SIZE bytes
            .chunks(PIXEL_BYTE_SIZE) // Process the remaining data in chunks of PIXEL_BYTE_SIZE bytes (RGBA)
            .all(|pixel: &[u8]| pixel == &message_data[BMP_HEADER_SIZE..BMP_HEADER_SIZE+PIXEL_BYTE_SIZE]); // Compare each chunk with the first pixel (after BMP_HEADER_SIZE bytes)

        // Assert the message content length
        let message_length: usize = message_data.len();
        dbg!(message_length);
        assert!(
            message_length > 0,
            "Expected WebSocket message length > 0, but got {}",
            message_length
        );
    }

    assert!(
        is_bitmap_not_empty,
        "Expected bitmap to not be empty, but got {}",
        is_bitmap_not_empty
    );

    // Clean up: Stop the server after the test
    server_handle.abort();
}
