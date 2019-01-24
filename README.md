# ImageProcessor

An object-oriented Java library for processing images.

## Examples

### Loading and saving images
Load an image into an `ImageProcessor`:
```Java
ImageProcessor ipr = new ImageProcessor(new File("examples/landscape.jpg"));
```

Save an image to a file:
```Java
ipr
    // ... chain functions here
    .saveAs(new File("path/to/file.png"), "png");
```

Storing an image as a `BufferedImage`:
```Java
BufferedImage processed = ipr
    // ... chain functions here
    .extract();
```

### Using and chaining functions
```Java
// Apply a gradient map
ipr
    .gradientMap(0.85, 
        new ColorPos(Color.BLUE.darker().darker(), 0),
        new ColorPos(210, 110, 60, 1))
    .saveAs(new File("examples/landscape-old.jpg"), "jpg");
```
![](https://raw.githubusercontent.com/fergusch/image-processor/master/examples/landscape-old.jpg)
```Java
// Apply a red tint
ipr
    .tint(new Color(255, 0, 0), 0.4f)
    .saveAs(new File("examples/landscape-tint.jpg"), "jpg");
```
![](https://raw.githubusercontent.com/fergusch/image-processor/master/examples/landscape-tint.jpg)
```Java
// Invert the image to its negative and grayscale it
ipr
    .negative()
    .grayscale()
    .saveAs(new File("examples/landscape-negative.jpg"), "jpg");
```
![](https://raw.githubusercontent.com/fergusch/image-processor/master/examples/landscape-negative.jpg)
```Java
// Overlay an image and mirror it
ipr.overlay(
        new ImageProcessor(new File("examples/overlays/sun.png")).extract(), 
        new Point(20, 20), // x-y coordinates
        20.0, // angle of rotation
        2.0, // scale
        0.7f // alpha
    )
    .mirror(Direction.HORIZONTAL)
    .saveAs(new File("examples/overlayed.jpg"), "jpg");
```
![](https://raw.githubusercontent.com/fergusch/image-processor/master/examples/overlayed.jpg)

## List of functions
- Add noise `addNoise()`
- Crop `crop()`
- Draw text `drawText()`
- Gradient map `gradientMap()`
- Grayscale `grayscale()`
- Mirror `mirror()`
- Negative `negative()`
- Overlay image `overlay()`
- Resize `resizeTo()`
- Rotate `rotate()`
- Brightness/contrast `scaleSamples()`
- Tint `tint()`