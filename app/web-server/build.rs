// build.rs
fn main() {
    cc::Build::new()
        .cpp(true)
        .file("src/lib.cpp")
        .compile("libcpp");

    println!("cargo:rustc-link-search=native=.");
    println!("cargo:rustc-link-lib=dylib=cpp");
}
