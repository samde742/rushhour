package rushhour;

import java.awt.Rectangle;

class Button extends Rectangle {

    int x,y,w,h, aW, aH;

    Button(int x, int y, int w, int h, int aW, int aH) {
        this.x = x; this.y = y;
        this.w = w; this.h = h;
        this.aH = aH; this.aW = aW;
    }
}