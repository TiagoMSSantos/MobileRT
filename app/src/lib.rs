#![no_std]
#[no_mangle]
pub extern "C" fn add(a: i32, b: i32) -> i32 {
  println!("Hello, world!");
}
