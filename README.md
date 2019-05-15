# Seam-Carving

This project was built as part of "Graphics basics and image processing", computer science course in Tel-Aviv University.
<br /> This project implements the algorithm according to the Seam carving paper [here]( http://www.faculty.idc.ac.il/arik/papers/imret.pdf)

### Algorithm overview
Seam carving is a novel algorithm for resizing images while maintaining as much
information as possible from the source image. A "seam" in this context is an 8-
connected path of pixels from the top of the image to the bottom or from the left to
the right.
<br /> Seam carving uses a dynamic programming method to compute a directed energy
map over the image. Using this map it finds a seam with the least energy. Removing
this seam produces a smaller image and applying this process repeatedly allows
reducing the size of the image freely and changing its aspect ratio.
<br />In order to increase the size of the image, seam carving finds k seams with the least
energy and duplicates them in ascending order.

### Straight seam and General seam
[original image](https://github.com/NoiCoh/Seam-Carving/blob/master/Image/1.jpg?raw=true)
<br />
 <img src="https://github.com/NoiCoh/Seam-Carving/blob/master/Image/straight_seam_1.jpg?raw=true" width="250" hover title="straight seam">  <img src="https://github.com/NoiCoh/Seam-Carving/blob/master/Image/general_seam_1.jpg?raw=true" width="250" hover title="general seam">
### Special features
-  **Improving image enlarging-** we blended the added seam by interpolation with its neighbors

<img src="https://github.com/NoiCoh/Seam-Carving/blob/master/Image/orig_2.jpg?raw=true" width="250" hover title="general seam">  <img src="https://github.com/NoiCoh/Seam-Carving/blob/master/Image/interp_2.jpg?raw=true" width="250" hover title="Improved">
- **Forward Energy**- we implemented this method according to [this](http://www.faculty.idc.ac.il/arik/SCWeb/vidret/vidretLowRes.pdf) paper (Section 5)

 <img src="https://github.com/NoiCoh/Seam-Carving/blob/master/Image/backward_1.jpg?raw=true" width="250" hover title="backward energy">  <img src="https://github.com/NoiCoh/Seam-Carving/blob/master/Image/forward_1.jpg?raw=true" width="250" hover title="forward energy">
