# Blender tips

Used Blender **version 4** to place cameras in scenes.

## Position camera

* Place world viewport in the desired position to place a camera.
* Press `ctrl + alt + numpad 0` to place a new camera.
* Check the world coordinates of the camera in **Object Properties** of the placed camera.
* Move camera to the desired point to look at, and check wich coordinates are changed,
  * By doing this, the `lookAt` can be infered from the difference of both positions (before and after).

How to convert **Axis** from Blender (default) to MobileRT:

| Axis in Blender (default) | Axis in MobileRT                         |
|---------------------------|------------------------------------------|
| X                         | -X (Loader automatically converts to -X) |
| Y                         | -Z                                       |
| Z                         | Y                                        |

Check [Blender documentation](https://docs.blender.org/manual/en/latest/editors/3dview/navigate/camera_view.html#camera-positioning).

## Create camera file

E.g. of camera for San Miguel scene:

```text
t perspective
p 20.0 1.0 5.0
l 19.0 1.0 5.0
u 0.0 1.0 0.0
f 45 45
```

## Place single world light

* Select **Edit Mode** in **Layout menu**.
* Create a plan composed by 2 triangles.
  * Do not select other object to avoid merge 2 primitives into 1.
  * Provide X, Y and Z coordinates.
  * Provide size to scale it and cover the desired scene.
  * Place it above the scene and make it face the scene.
  * Enable **Face Orientation** in **Viewport Overlay menu** to know where primitives are facing.
    * Blue is front side and red is back side.
* Export the scene into a new Wavefront OBJ file.

E.g. of world light position for San Miguel scene:

```text
o PlaneMobileRT
v 5000 20 5000
v -5000 20 5000
v 5000 20 -5000
v -5000 20 -5000
vn 0 -1 0
vt 0 0
vt 1 0
vt 1 1
vt 0 1
s 0
usemtl LightMobileRT
f 5933232/2220965/9754200 5933233/2220966/9754200 5933235/2220967/9754200 5933234/2220968/9754200
```

And the material:

```text
newmtl LightMobileRT
Ns 250
Ka 1 1 1
Kd 0.8 0.8 0.8
Ks 0.5 0.5 0.5
Ke 1 1 1
Ni 1.45
d 1
illum 2
```
