# Ray tracer for Android

![alt text](Example.gif)

## TODO

### Ray tracing engine
- [ ] Implement loading of textures
- [ ] Separate Material from Primitive in order to save memory
- [ ] Implement KD-Tree
- [ ] Improve BVH
- [ ] Add ray packet intersections
- [ ] Add gpu ray tracing support for comparison
- [ ] Add more types of shapes
- [ ] Support more types of models besides .obj files

### Ray tracing JNI layer
- [ ] Refactor DrawView translation unit

### Ray tracing shaders
- [ ] Add Bidirectional Path Tracing
- [ ] Add Metropolis light transport
- [ ] Add shader for debug purposes (wireframe of shapes and boxes)

### Ray tracing test cases
- [ ] Prepare more scene models with Blender for testing

### User Interface
- [x] Fix memory leak in Java UI
- [x] Fix load of obj files in Android 10
- [ ] Change Linux's UI from GTK to Qt

### System
- [x] Add comments in the Android UI
- [ ] Add comments in the JNI layer
- [ ] Add comments in the MobileRT
- [x] Give out of memory error when the memory is not enough to load the scene
- [ ] Add unit tests (more code coverage)
- [x] Add instrumented unit tests
- [ ] Add git hooks to check git commit messages
- [ ] Add git hooks to submit Jenkins' jobs after each git push
- [ ] Support to export rendered image to file
- [x] Add CI / CD support from github (actions) for the Google Test unit tests

### Documentation
- [x] Improve README
- [ ] Write documentation
- [ ] Update gif image
- [ ] Benchmark against popular ray tracers like PBRT
