use anyhow::Result;

#[tokio::main]
async fn main() -> Result<()> {
    web_server::start_http_server().await.await.unwrap();

    println!("finished http_server");
    Ok(())
}
