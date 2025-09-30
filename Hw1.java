/*
Alex Bryant
bryan140@pnw.edu
Course: 45500 Computer Graphics
Homework Assignment 1
*/

import framebuffer.FrameBuffer;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
   Outline of CS 45500 Assignment 1.
*/
public class Hw1
{
   public static void main(String[] args)
   {
      // Use a properties file to find out
      // which PPM files to use as assets.
      final Properties properties = new Properties();
      try
      {
         properties.load(
            new FileInputStream(
               new File("assets.properties")));
      }
      catch (IOException e)
      {
         e.printStackTrace(System.err);
         System.exit(-1);
      }
      final String file_1 = properties.getProperty("file1"); // 1st ppm file
      final String file_2 = properties.getProperty("file2"); // 2nd ppm file

      // This framebuffer holds the image from the first ppm file.
      final FrameBuffer fbEmbedded = new FrameBuffer(file_1);

      /******************************************/

      // Your code goes here.
      //  1. Create a 1100-by-700 framebuffer with the darker background color.
      final int WIDTH = 1100;
      final int HEIGHT = 700;
      final Color DARK_BG = new Color(192, 52, 14);
      final FrameBuffer fb = new FrameBuffer(WIDTH, HEIGHT);
      fb.clearFB(DARK_BG);
      //  2. Create a 1000-by-600 viewport filled with the lighter background color.
      final int viewpoint_x = 48, viewpoint_y = 48;
      final int viewpoint_w = 1000, viewpoint_h = 600;
      final Color LIGHT_BG = new Color(255, 189, 96);
      FrameBuffer.Viewport vp = fb.new Viewport(viewpoint_x, viewpoint_y, viewpoint_w, viewpoint_h);
      vp.clearVP(LIGHT_BG);
      //  3. Draw the horizontal and vertical grid lines (each is 4 pixels wide).
      final int GRID_SPACING = 100;
      final int LINE_THICKNESS = 4;
      final Color GRID_COLOR = new Color(192, 52, 14);

      for (int x = 0; x < viewpoint_w; x += GRID_SPACING) {
         for(int dx = 0; dx < LINE_THICKNESS && x + dx < viewpoint_w; dx++) {
            for(int y = 0; y < viewpoint_h; y++) {
               vp.setPixelVP(x + dx, y, GRID_COLOR);
            }
         }
      }
      for (int y = 0; y < viewpoint_h; y += GRID_SPACING) {
         for(int dy = 0; dy < LINE_THICKNESS && y + dy < viewpoint_h; dy++) {
            for(int x = 0; x < viewpoint_w; x++) {
               vp.setPixelVP(x, y + dy, GRID_COLOR);
            }
         }
      }
   
      //  4. Put diagonal lines at the corners of the framebuffer (each is 3 pixel wide)
// 4. Corner diagonals that meet the LIGHT_BG just inside the grid border (3 px wide).
final Color DIAG_COLOR = new Color(255, 189, 96);

// last valid outer-corner pixels
final int TL_OUT_X = 0,          TL_OUT_Y = 0;
final int TR_OUT_X = WIDTH - 1,  TR_OUT_Y = 0;
final int BL_OUT_X = 0,          BL_OUT_Y = HEIGHT - 1;
final int BR_OUT_X = WIDTH - 1,  BR_OUT_Y = HEIGHT - 1;

// inner targets = first LIGHT_BG pixels past the grid border
// (grid border at viewport edge is LINE_THICKNESS wide)
final int LEFT_IN   = viewpoint_x + LINE_THICKNESS;
final int RIGHT_IN  = viewpoint_x + viewpoint_w - 1 - LINE_THICKNESS;
final int TOP_IN    = viewpoint_y + LINE_THICKNESS;
final int BOTTOM_IN = viewpoint_y + viewpoint_h - 1 - LINE_THICKNESS;

// helper: draw 3-px thick 45° line from (x0,y0) to (x1,y1) with two inward offsets
java.util.function.Consumer<int[]> draw = seg -> {
    int x0 = seg[0], y0 = seg[1], x1 = seg[2], y1 = seg[3];
    int sx = (x1 >= x0) ? 1 : -1;
    int sy = (y1 >= y0) ? 1 : -1;
    int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));

    int offAx = seg[4], offAy = seg[5];
    int offBx = seg[6], offBy = seg[7];

    for (int i = 0; i <= steps; i++) {          // include endpoint
        int x = x0 + sx * i, y = y0 + sy * i;

        if (0 <= x && x < WIDTH && 0 <= y && y < HEIGHT) fb.setPixelFB(x, y, DIAG_COLOR);

        int xA = x + offAx, yA = y + offAy;     // keep thickness inside the border
        if (0 <= xA && xA < WIDTH && 0 <= yA && yA < HEIGHT) fb.setPixelFB(xA, yA, DIAG_COLOR);

        int xB = x + offBx, yB = y + offBy;
        if (0 <= xB && xB < WIDTH && 0 <= yB && yB < HEIGHT) fb.setPixelFB(xB, yB, DIAG_COLOR);
    }
};

// TOP-LEFT: inward = right & down → end at (LEFT_IN, TOP_IN)
draw.accept(new int[]{ TL_OUT_X, TL_OUT_Y, LEFT_IN,  TOP_IN,   +1, 0,  0, +1 });
// TOP-RIGHT: inward = left & down → end at (RIGHT_IN, TOP_IN)
draw.accept(new int[]{ TR_OUT_X, TR_OUT_Y, RIGHT_IN, TOP_IN,   -1, 0,  0, +1 });
// BOTTOM-LEFT: inward = right & up → end at (LEFT_IN, BOTTOM_IN)
draw.accept(new int[]{ BL_OUT_X, BL_OUT_Y, LEFT_IN,  BOTTOM_IN, +1, 0,  0, -1 });
// BOTTOM-RIGHT: inward = left & up → end at (RIGHT_IN, BOTTOM_IN)
draw.accept(new int[]{ BR_OUT_X, BR_OUT_Y, RIGHT_IN, BOTTOM_IN, -1, 0,  0, -1 });

      
      //  5. Create a viewport and draw into it the checkered pattern.
final int TILE       = 30;
final int CHECK_COLS = 9;                 // 300 px wide
final int CHECK_ROWS = 6;                  // 180 px tall

final int checker_w = CHECK_COLS * TILE;   // 300
final int checker_h = CHECK_ROWS * TILE;   // 180
final int checker_x = viewpoint_x + viewpoint_w - checker_w - 168;  // right edge aligned
final int checker_y = viewpoint_y + 372;                       // vertical placement

FrameBuffer.Viewport vpCheck = fb.new Viewport(checker_x, checker_y, checker_w, checker_h);

// colors
final Color RED   = new Color(241, 95, 116);  // #F15F74
final Color GREEN = new Color(152, 203, 74);  // #98CB4A
final Color BLUE  = new Color(84, 129, 230);  // #5481E6

for (int y = 0; y < checker_h; y++) {
    int ry = y / TILE;
    for (int x = 0; x < checker_w; x++) {
        int rx = x / TILE;
        int t = (rx + ry) % 3;                   // RED → GREEN → BLUE
        Color c = (t == 0) ? RED : (t == 1) ? GREEN : BLUE; vpCheck.setPixelVP(x, y, c);
    }
}

      //  6. Create a viewport and draw into it the striped disk pattern.
      final int disk_x = viewpoint_x + 101;
      final int disk_y = viewpoint_y + 301;
      final int disk_w = 303, disk_h = 303;

      FrameBuffer.Viewport vpDisk = fb.new Viewport(disk_x, disk_y, disk_w, disk_h);

final int cx = disk_w / 2, cy = disk_h / 2;
final int R  = Math.min(disk_w, disk_h) / 2 - 1;
final int RING_W = 30;  // each ring is 30 pixels wide

for (int y = 0; y < disk_h; y++) {
    int dy = y - cy;
    for (int x = 0; x < disk_w; x++) {
        int dx = x - cx;
        int d2 = dx * dx + dy * dy;
        if (d2 <= R * R) {
            double d = Math.sqrt(d2);                     // distance to center
            int k = (int) ((R - d) / RING_W);             // ring index from outside in
            if (k >= 0 && k < 3) {                         // draw only 3 outer rings
                Color c = (k == 0) ? BLUE : (k == 1) ? RED : GREEN;
                vpDisk.setPixelVP(x, y, c);
            }
            // else: leave the inner area unpainted to show the light background
        }
    }
}
      //  7. Create a viewport and copy into it a flipped copy of the first ppm file.
      
final int imgW  = fbEmbedded.getWidthFB();
final int imgH  = fbEmbedded.getHeightFB();
final int tol   = 3;  // Taking off white background

final int copyW = Math.min(imgW, viewpoint_w);
final int copyH = Math.min(imgH, viewpoint_h);

// placement inside the light background
final int flip_x = viewpoint_x + 283;
final int flip_y = viewpoint_y + 77;

FrameBuffer.Viewport vpFlip = fb.new Viewport(flip_x, flip_y, copyW, copyH);

// Horizontal flip: sample from right to left
for (int y = 0; y < copyH; y++) {
    for (int x = 0; x < copyW; x++) {
        int srcX = imgW - 1 - x;  // flip X
        int srcY = y;
        Color src = fbEmbedded.getPixelFB(srcX, srcY);

        boolean nearlyWhite =
            src.getRed()   >= 255 - tol &&
            src.getGreen() >= 255 - tol &&
            src.getBlue()  >= 255 - tol;

        if (nearlyWhite) continue;          // skip: let background/grid show through
        vpFlip.setPixelVP(x, y, src);       // draw only the non-white trooper pixels
    }
}

      //  8. Create another viewport copy into it another flipped copy of the first ppm file.


// 8. Another flipped copy (vertical mirror) with transparency for nearly-white pixels
// reuse imgW, imgH, copyW, copyH, tol from above

final int flip2_x = viewpoint_x + 27;
final int flip2_y = viewpoint_y + 77;

FrameBuffer.Viewport vpFlip2 = fb.new Viewport(flip2_x, flip2_y, copyW, copyH);

// Vertical flip: sample from bottom to top
for (int y = 0; y < copyH; y++) {
    for (int x = 0; x < copyW; x++) {
        int srcX = x;
        int srcY = imgH - 1 - y;  // flip Y
        Color src = fbEmbedded.getPixelFB(srcX, srcY);

        boolean nearlyWhite =
            src.getRed()   >= 255 - tol &&
            src.getGreen() >= 255 - tol &&
            src.getBlue()  >= 255 - tol;

        if (nearlyWhite) continue;          // skip: keep background/grid visible
        vpFlip2.setPixelVP(x, y, src);
    }
}

// 9) Source viewport that covers the 6 (3×2) grid squares to be copied.
final int startCol   = 3;                   // leftmost column of the 3×2 block
final int startRow   = 2;                   // top row of the 3×2 block
final int cellsWide  = 3;                   // 3 columns
final int cellsHigh  = 2;                   // 2 rows

// Top-left pixel of the block inside the LIGHT background panel
final int copy_x = viewpoint_x + startCol * GRID_SPACING;
final int copy_y = viewpoint_y + startRow * GRID_SPACING;

// Width/height of that block; subtract the trailing grid line thickness
final int copy_w = cellsWide * GRID_SPACING - LINE_THICKNESS;
final int copy_h = cellsHigh * GRID_SPACING - LINE_THICKNESS;

// This viewport *views* the 3×2 region we’re going to copy
FrameBuffer.Viewport vpSix = fb.new Viewport(copy_x, copy_y, copy_w, copy_h);



// 10) Create another viewport to hold "framed" copy of the previous viewport.


// Source block from #9
int srcW = copy_w;
int srcH = copy_h;

// ---- Frame controls (tweak these) ----
final int FRAME_OUT_W  = 250;   // outer width of the frame (pixels)
final int FRAME_OUT_H  = 350;   // outer height of the frame (pixels) -> taller than width = portrait
final int EDGE_THICK   = 25;    // border thickness ("firmness")
final int FRAME_MARGIN = 22;    // distance from the light panel edges
final Color FRAME_EDGE = new Color(192, 192, 192);   // frame color
// --------------------------------------

// Place at top-right of the light background
final int frame_x = viewpoint_x + viewpoint_w - FRAME_OUT_W - FRAME_MARGIN - 1;
final int frame_y = viewpoint_y + FRAME_MARGIN - 20;

// Create the frame viewport
FrameBuffer.Viewport vpFrame = fb.new Viewport(frame_x, frame_y, FRAME_OUT_W, FRAME_OUT_H);

// Draw ONLY the border (leave inner opening transparent so panel shows through)
for (int t = 0; t < EDGE_THICK; t++) {
    // top & bottom
    for (int x = 0; x < FRAME_OUT_W; x++) {
        vpFrame.setPixelVP(x, t,                    FRAME_EDGE);
        vpFrame.setPixelVP(x, FRAME_OUT_H - 1 - t,  FRAME_EDGE);
    }
    // left & right
    for (int y = 0; y < FRAME_OUT_H; y++) {
        vpFrame.setPixelVP(t,                    y, FRAME_EDGE);
        vpFrame.setPixelVP(FRAME_OUT_W - 1 - t, y, FRAME_EDGE);
    }
}

// (In #11 you'll create an inner viewport of size copy_w x copy_h at (FRAME_PAD, FRAME_PAD)

// Size of the inner opening (area inside the gray border)
final int openW = FRAME_OUT_W - 2 * EDGE_THICK;
final int openH = FRAME_OUT_H - 2 * EDGE_THICK;

// Pick where to sample FROM in the scene.
// Use the trooper image you placed earlier as an anchor.
// If your trooper came from step #7, these exist: flip_x, flip_y (top-left of that image).
// If you used a different one (e.g., vpFlipH), replace flip_x/flip_y accordingly.
final int SRC_ANCHOR_X = flip_x;   // top-left x of the trooper image in the framebuffer
final int SRC_ANCHOR_Y = flip_y;   // top-left y of the trooper image in the framebuffer

// Offsets to slide the crop over the trooper until it matches your reference.
// Tweak these two numbers only.
final int SRC_OFFSET_X = - 81;      // move window right/left over the trooper
final int SRC_OFFSET_Y =  25;      // move window up/down over the trooper

// Source crop (same size as the opening so it fits exactly)
srcW = openW;
srcH = openH;
int srcX = SRC_ANCHOR_X + SRC_OFFSET_X;
int srcY = SRC_ANCHOR_Y + SRC_OFFSET_Y;

// Clamp crop to the framebuffer so we never read out-of-bounds
if (srcX < 0) srcX = 0;
if (srcY < 0) srcY = 0;
if (srcX + srcW > WIDTH)  srcX = Math.max(0, WIDTH  - srcW);
if (srcY + srcH > HEIGHT) srcY = Math.max(0, HEIGHT - srcH);

// Make a viewport that "looks at" the source crop area
FrameBuffer.Viewport vpSrcWindow = fb.new Viewport(srcX, srcY, srcW, srcH);

// Copy that window into the frame's inner opening, unrotated
for (int y = 0; y < srcH; y++) {
    for (int x = 0; x < srcW; x++) {
        Color c = vpSrcWindow.getPixelVP(x, y);
        // write into the frame viewport, offset by the border thickness
        vpFrame.setPixelVP(EDGE_THICK + x, EDGE_THICK + y, c);
    }
}


   final int FRAME_PAD = 3;
      // 12. Load Dumbledore (the second ppm file) into another FrameBuffer.
      final FrameBuffer fbDumbledore = new FrameBuffer(file_2);
      
      // 13. Create a viewport to hold Dumbledore's ghost.
final int GHOST_MARGIN = 20;

final int ghostW = Math.min(fbDumbledore.getWidthFB(),  viewpoint_w - 2 * GHOST_MARGIN);
final int ghostH = Math.min(fbDumbledore.getHeightFB(), viewpoint_h - 2 * GHOST_MARGIN);

// center inside the 1000x600 light panel
final int ghost_x = viewpoint_x + (viewpoint_w - ghostW) / 2 + 102;
final int ghost_y = viewpoint_y + (viewpoint_h - ghostH) / 2 + 2;

final FrameBuffer.Viewport vpGhost = fb.new Viewport(ghost_x, ghost_y, ghostW, ghostH);

      // 14. Blend Dumbledore from its framebuffer into the viewport from step 13.
      final int WHITE_TOL = 3;       // tweak 6–12 as needed
final float W_SRC = 0.6f;      // Dumbledore
final float W_BG  = 0.4f;      // background

for (int y = 0; y < ghostH; y++) {
    for (int x = 0; x < ghostW; x++) {
        // source from Dumbledore image
        Color c1 = fbDumbledore.getPixelFB(x, y);

        // skip if nearly white (let background show through)
        boolean nearlyWhite = c1.getRed()   >= 255 - WHITE_TOL &&
                              c1.getGreen() >= 255 - WHITE_TOL &&
                              c1.getBlue()  >= 255 - WHITE_TOL;
        if (nearlyWhite) continue;

        // background already drawn on the main framebuffer
        Color c2 = fb.getPixelFB(ghost_x + x, ghost_y + y);

        int r = Math.round(W_SRC * c1.getRed()   + W_BG * c2.getRed());
        int g = Math.round(W_SRC * c1.getGreen() + W_BG * c2.getGreen());
        int b = Math.round(W_SRC * c1.getBlue()  + W_BG * c2.getBlue());

        // write blended pixel into the ghost viewport
        vpGhost.setPixelVP(x, y, new Color(r, g, b));
    }
}
 
 /******************************************/
      // Save the resulting image in a file.
      final String savedFileName = "Hw1.ppm";
      fb.dumpFB2File( savedFileName );
      System.err.println("Saved " + savedFileName);
   }
}
