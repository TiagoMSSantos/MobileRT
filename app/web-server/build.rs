fn main() {
    cc::Build::new()
        .cpp(true)
        .file("src/lib.cpp")
        .include("../")
        .include("../System_dependent/Native")
        .include("../third_party")
        .include("../third_party/glm")
        .include("../third_party/pcg-cpp/include")
        .include("../third_party/boost/libs/assert/include")
        .include("../third_party/boost/libs/sort/include")
        .include("../third_party/boost/libs/type_traits/include")
        .include("../third_party/boost/libs/config/include")
        .include("../third_party/boost/libs/static_assert/include")
        .include("../third_party/boost/libs/core/include")
        .include("../third_party/boost/libs/range/include")
        .include("../third_party/boost/libs/preprocessor/include")
        .include("../third_party/boost/libs/mpl/include")
        .include("../third_party/boost/libs/iterator/include")
        .compile("libcpp");

    println!("cargo:rustc-link-search=native=.");
    println!("cargo:rustc-link-search=native=../../build_release/lib");
    println!("cargo:rustc-link-lib=dylib=cpp");
    println!("cargo:rustc-link-lib=dylib=Components");
    println!("cargo:rustc-link-lib=dylib=MobileRT");
}
