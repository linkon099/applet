import java.applet.*;
import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.net.URL;
/* <APPLET CODE="fivestar.java" WIDTH="400" HEIGHT="400" ALIGN="middle">
</APPLET>
*/

public class fivestar extends Applet
{

    public void init()
    {
        resize(400, 400);
        MediaTracker tracker = new MediaTracker(this);
        boardimage = getImage(getDocumentBase(), "paboard.gif");
        tracker.addImage(boardimage, 0);
        pieceimage = getmarble();
        piecekeeper = new Point[9];
        endpointarray = new Point[10];
        endpointarray[0] = OUT1;
        endpointarray[1] = OUT2;
        endpointarray[2] = OUT3;
        endpointarray[3] = OUT4;
        endpointarray[4] = OUT5;
        endpointarray[5] = IN1;
        endpointarray[6] = IN2;
        endpointarray[7] = IN3;
        endpointarray[8] = IN4;
        endpointarray[9] = IN5;
        homearray = new Point[9];
        homearray[0] = START0;
        homearray[1] = START1;
        homearray[2] = START2;
        homearray[3] = START3;
        homearray[4] = START4;
        homearray[5] = START5;
        homearray[6] = START6;
        homearray[7] = START7;
        homearray[8] = START8;
        movechecker = new boolean[3];
        linearitycheck = new Point[3];
        for(int i = 0; i < 3; i++)
        {
            movechecker[i] = false;
            linearitycheck[i] = new Point(0, 0);
        }

        for(int i = 0; i < 9; i++)
            piecekeeper[i] = new Point(homearray[i].x, homearray[i].y);

       try
        {
            showStatus("Hang on--images are loading.");
            tracker.waitForID(0);
        }
        catch(InterruptedException _ex)
        {
            return;        }
         String s = getCodeBase().getHost();

    }

    public void start()
    {
        for(int i = 0; i < 9; i++)
        {
            piecekeeper[i].x = homearray[i].x;
            piecekeeper[i].y = homearray[i].y;
        }

        for(int i = 0; i < 3; i++)
        {
            movechecker[i] = false;
            linearitycheck[i].x = 0;
            linearitycheck[i].y = 0;
        }

        movingPiece = 10;
        thecount = 0;
    }

    public void destroy()
    {
        boardimage.flush();
    }

    public void update(Graphics g)
    {
        paint(g);
    }

    public void paint(Graphics g)
    {
        g.drawImage(boardimage, 0, 0, this);
        for(int i = 0; i < 10; i++)
            paintRedDot(g, endpointarray[i]);

        for(int i = 0; i < 9; i++)
        {
            Point p = piecekeeper[i];
            if(i != movingPiece)
                g.drawImage(pieceimage, p.x - 20, p.y - 20, this);
        }

        if(movingPiece != 10)
        {
            Point p = piecekeeper[movingPiece];
            g.drawImage(pieceimage, p.x - 20, p.y - 20, this);
        }
    }

    private void paintRedDot(Graphics g, Point p)
    {
        int R = 4;
        Color lastcolor = g.getColor();
        Color dullred = new Color(0x800000);
        g.setColor(dullred);
        g.fillOval(p.x - R, p.y - R, R * 2, R * 2);
        g.setColor(lastcolor);
    }

   public boolean mouseDown(Event e, int x, int y)
{
          if(gameOver)
        {
            gameOver = false;
            start();
            repaint();
           return true;
        }
        movingPiece = 10;
        Point clickPoint = new Point(x, y);
        for(int i = 0; i < 9; i++)
        {
            if(!pointNearPoint(clickPoint, piecekeeper[i], 20))
                continue;
            movingPiece = i;
            lastPosition.x = x;
            lastPosition.y = y;
            break;
        }

        for(int i = 0; i < 10; i++)
            if(piecekeeper[movingPiece].equals(endpointarray[i]))
                movingPiece = 10;

        if(movingPiece == 10)
            return super.mouseDown(e, x, y);
        else
            return true;
    }

    private boolean pointNearPoint(Point test, Point center, int radius)
    {
        int xdiff = test.x - center.x;
        int ydiff = test.y - center.y;
        return xdiff * xdiff + ydiff * ydiff <= radius * radius;
    }

    public boolean mouseDrag(Event e, int x, int y)
    {
        if(movingPiece == 10)
            return super.mouseDrag(e, x, y);
        if(offscreen == null)
            offscreen = createImage(400, 400);

        if(offscreen != null)
        {
            Rectangle oldRect = getRectAroundPoint(piecekeeper[movingPiece], 20);
            int xdist = x - lastPosition.x;
            int ydist = y - lastPosition.y;
            piecekeeper[movingPiece].x += xdist;
            piecekeeper[movingPiece].y += ydist;
            Rectangle newRect = getRectAroundPoint(piecekeeper[movingPiece], 20);
            Rectangle r = newRect.union(oldRect);
            Graphics g = offscreen.getGraphics();
            g.clipRect(r.x, r.y, r.width, r.height);
            paint(g);
            g = getGraphics();
            g.clipRect(r.x, r.y, r.width, r.height);
            g.drawImage(offscreen, 0, 0, this);
            lastPosition.x += xdist;
            lastPosition.y += ydist;
        }
        Point newpoint = new Point(piecekeeper[movingPiece].x, piecekeeper[movingPiece].y);
        for(int i = 0; i < 10; i++)
            if(pointNearPoint(newpoint, endpointarray[i], 20))
                switch(thecount)
                {
                case 0: // '\0'//
                    if(!endpointIsOccupied(i))
                    {
                        thecount++;
                        movechecker[0] = true;
                        linearitycheck[0].x = endpointarray[i].x;
                        linearitycheck[0].y = endpointarray[i].y;
                    }
                    break;

                case 1: // '\001'//
                    if(!checkForDuplicateEndpoint(endpointarray[i]))
                    {
                        thecount++;
                        movechecker[1] = true;
                        linearitycheck[1].x = endpointarray[i].x;
                        linearitycheck[1].y = endpointarray[i].y;
                    }
                    break;

                case 2: // '\002'//
                    if(!checkForDuplicateEndpoint(endpointarray[i]) && !endpointIsOccupied(i))
                    {
                        thecount++;
                        movechecker[2] = true;
                        linearitycheck[2].x = endpointarray[i].x;
                        linearitycheck[2].y = endpointarray[i].y;
                    }
                    break;

                default:
                    if(!checkForDuplicateEndpoint(endpointarray[i]))
                        movechecker[0] = false;
                    break;
                }

        showStatus(" " + thecount);
        return true;
    }

    private boolean checkForDuplicateEndpoint(Point aPoint)
    {
        for(int i = 0; i < 3; i++)
            if(aPoint.equals(linearitycheck[i]))
                return true;

        return false;
    }

    private Rectangle getRectAroundPoint(Point center, int radius)
    {
        Rectangle r = new Rectangle(center.x - radius, center.y - radius, radius * 2, radius * 2);
        return r;
    }

    private boolean endpointIsOccupied(int pointindex)
    {
        for(int i = 0; i < 9; i++)
            if(endpointarray[pointindex].equals(piecekeeper[i]))
                return true;

     return false;
    }

    public boolean mouseUp(Event e, int x, int y)
    {
        if(movingPiece == 10)
            return super.mouseDrag(e, x, y);
        for(int i = 0; i < 10; i++)
            if(pointNearPoint(endpointarray[i], piecekeeper[movingPiece], 20))
            {
                if(offscreen == null)
                    offscreen = createImage(400, 400);
                if(offscreen!= null)
                {
                    Rectangle oldRect = getRectAroundPoint(piecekeeper[movingPiece], 20);
                    piecekeeper[movingPiece].x = endpointarray[i].x;
                    piecekeeper[movingPiece].y = endpointarray[i].y;
                    Rectangle newRect = getRectAroundPoint(piecekeeper[movingPiece], 20);
                    Rectangle r = newRect.union(oldRect);
                    Graphics g = offscreen.getGraphics();
                    g.clipRect(r.x, r.y, r.width, r.height);
                    paint(g);
                    g = getGraphics();
                    g.clipRect(r.x, r.y, r.width, r.height);
                    g.drawImage(offscreen, 0, 0, this);
                }
            }

        boolean badmove = false;
        if(thecount != 3)
            badmove = true;
        for(int i = 0; i < 3; i++)
        {
            if(movechecker[i])
                continue;
            badmove = true;
            break;
        }

        if(!checkLinearity())
            badmove = true;
        if(badmove)
        {
           if(offscreen == null)
                offscreen = createImage(400, 400);
            if(offscreen != null)
            {
                Rectangle oldRect = getRectAroundPoint(piecekeeper[movingPiece], 20);
                piecekeeper[movingPiece].x = homearray[movingPiece].x;
                piecekeeper[movingPiece].y = homearray[movingPiece].y;
                Rectangle newRect = getRectAroundPoint(piecekeeper[movingPiece], 20);
                Rectangle r = newRect.union(oldRect);
                Graphics g = offscreen.getGraphics();
                g.clipRect(r.x, r.y, r.width, r.height);
                paint(g);
                g = getGraphics();
                g.clipRect(r.x, r.y, r.width, r.height);
                g.drawImage(offscreen, 0, 0, this);
            }
        }
        for(int i = 0; i < 3; i++)
        {
            movechecker[i] = false;
            linearitycheck[i].x = 0;
            linearitycheck[i].y = 0;
        }

        thecount = 0;
        lastPosition.x = 0;
        lastPosition.y = 0;
        checkForWin();
        return true;
    }

    private boolean checkLinearity()
    {
        if(linearitycheck[0].equals(OUT1))
            if(linearitycheck[1].equals(IN1))
            {
                if(linearitycheck[2].equals(IN2))
                    return true;
            } else
            if(linearitycheck[1].equals(IN5) && linearitycheck[2].equals(IN4))
                return true;
        if(linearitycheck[0].equals(OUT2))
            if(linearitycheck[1].equals(IN1))
            {
                if(linearitycheck[2].equals(IN5))
                    return true;
            } else
            if(linearitycheck[1].equals(IN2) && linearitycheck[2].equals(IN3))
                return true;
        if(linearitycheck[0].equals(OUT3))
            if(linearitycheck[1].equals(IN2))
            {
                if(linearitycheck[2].equals(IN1))
                    return true;
            } else
            if(linearitycheck[1].equals(IN3) && linearitycheck[2].equals(IN4))
                return true;
        if(linearitycheck[0].equals(OUT4))
            if(linearitycheck[1].equals(IN3))
            {
                if(linearitycheck[2].equals(IN2))
                    return true;
            } else
            if(linearitycheck[1].equals(IN4) && linearitycheck[2].equals(IN5))
                return true;
        if(linearitycheck[0].equals(OUT5))
            if(linearitycheck[1].equals(IN5))
            {
                if(linearitycheck[2].equals(IN1))
                    return true;
            } else
            if(linearitycheck[1].equals(IN4) && linearitycheck[2].equals(IN3))
                return true;
        if(linearitycheck[0].equals(IN1))
            if(linearitycheck[1].equals(IN5))
            {
                if(linearitycheck[2].equals(OUT5))
                    return true;
            } else
            if(linearitycheck[1].equals(IN2) && linearitycheck[2].equals(OUT3))
                return true;
        if(linearitycheck[0].equals(IN2))
            if(linearitycheck[1].equals(IN1))
            {
                if(linearitycheck[2].equals(OUT1))
                    return true;
            } else
            if(linearitycheck[1].equals(IN3) && linearitycheck[2].equals(OUT4))
                return true;
        if(linearitycheck[0].equals(IN3))
            if(linearitycheck[1].equals(IN2))
            {
                if(linearitycheck[2].equals(OUT2))
                    return true;
            } else
            if(linearitycheck[1].equals(IN4) && linearitycheck[2].equals(OUT5))
                return true;
        if(linearitycheck[0].equals(IN4))
            if(linearitycheck[1].equals(IN3))
            {
                if(linearitycheck[2].equals(OUT3))
                    return true;
            } else
            if(linearitycheck[1].equals(IN5) && linearitycheck[2].equals(OUT1))
                return true;
        if(linearitycheck[0].equals(IN5))
            if(linearitycheck[1].equals(IN4))
            {
                if(linearitycheck[2].equals(OUT4))
                    return true;
            } else
            if(linearitycheck[1].equals(IN1) && linearitycheck[2].equals(OUT2))
                return true;
        return false;
    }

    private void checkForWin()
    {
        int filledendcount = 0;
        for(int i = 0; i < 10; i++)
            if(endpointIsOccupied(i))
                filledendcount++;

        if(filledendcount == 9)
        {
            Font font = new Font("Helvetica", 3, 24);
            String winner = new String("You did it!");
            Graphics g = getGraphics();
            g.setColor(Color.red);
            g.setFont(font);
            g.drawString(winner, 50, 195);
            gameOver = true;
        }
    }

    private Image getmarble()
    {
        int pw1array[] = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0xff003300, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0xff003300, 0xff666600, 
            0xff666600, 0xff663300, 0xff666600, 0xff663300, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0xff666600, 0xff996600, 0xff666600, 0xff666600, 0xff666600, 0xff999933, 
            0xff996600, 0xff999933, 0xff999900, 0xff666633, 0xff666600, 0xff333300, 0xff666600, 0xff663300, 0xff003300, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0xff666600, 0xff666600, 0xff999933, 0xff999900, 0xff999933, 0xff999933, 0xff999900, 0xff999900, 
            0xff999933, 0xff996600, 0xff666633, 0xff669900, 0xff666600, 0xff996633, 0xff666600, 0xff666600, 0xff666600, 0xff333300, 
            0xff333300, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0xff666600, 0xff999933, 0xff999900, 0xffcc9900, 0xff999933, 0xffcccc00, 0xff999900, 0xffcc9933, 0xffcc9933, 
            0xff999900, 0xff999933, 0xffcccc00, 0xffcc9933, 0xffcc9900, 0xff999900, 0xff999933, 0xff666600, 0xff999933, 0xff666600, 
            0xff663300, 0xff003300, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0xff666600, 
            0xff666600, 0xff999933, 0xff999900, -205, 0xff999933, 0xffcccc00, 0xffcc9933, 0xffcc9933, 0xff999900, 0xff999900, 
            0xffcccc33, 0xff999900, 0xffcc9933, 0xffcccc00, 0xff99cc33, 0xff999933, 0xff999900, 0xff996600, 0xff666600, 0xff666633, 
            0xff666600, 0xff663300, 0xff333300, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0xff666600, 0xff999900, 
            0xff999933, 0xffcccc00, 0xffcccc33, 0xffcccc00, 0xffcc9933, 0xffcc9900, 0xff99cc33, 0xff999900, 0xff999900, 0xff999933, 
            0xffcc9900, 0xffcccc33, 0xff999900, 0xffcc9933, 0xff999900, 0xff996600, 0xff999900, 0xff999933, 0xff999933, 0xff996600, 
            0xff666600, 0xff336633, 0xff666600, 0xff333300, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0xff666600, 0xff999900, 0xff999933, 
            0xffcc9900, 0xff999933, -13312, 0xff999933, 0xffcccc00, 0xff999933, 0xffcc9900, 0xffcccc33, 0xffcccc33, 0xffcc9900, 
            0xff99cc33, 0xffcc9900, 0xffcc9933, 0xff999900, 0xff999933, 0xff999900, 0xff666633, 0xff999900, 0xff999900, 0xff666600, 
            0xff666633, 0xff666600, 0xff663300, 0xff336600, 0xff333300, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0xff666600, 0xff996600, 0xff999933, 0xffcc9900, 
            0xff99cc33, 0xffcc9900, 0xffccff33, -13261, 0xffcccc33, 0xffcccc00, -13261, 0xffcccc00, 0xffcccc33, 0xffcc9900, 
            0xff999933, 0xffcc9900, 0xffcccc33, 0xff99cc00, 0xff999900, 0xff996633, 0xff999900, 0xff996600, 0xff996633, 0xff999900, 
            0xff666600, 0xff666600, 0xff666633, 0xff663300, 0xff333300, 0xff333300, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0xff666600, 0xff999933, 0xffcccc00, 0xffcccc33, 
            0xffcccc33, -205, 0xffcccc33, -13312, -205, 0xffcc9933, 0xffcccc00, 0xffcccc33, 0xffcc9900, 0xffcccc33, 
            0xffcc9900, 0xffcccc33, 0xff99cc00, 0xff999933, 0xff996600, 0xff669900, 0xff996633, 0xff669900, 0xff999900, 0xff666633, 
            0xff666600, 0xff666633, 0xff663300, 0xff336600, 0xff663300, 0xff333333, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0xff666600, 0xff999933, 0xffcc9900, 0xff999933, 0xffcccc33, 
            -13312, 0xff999900, 0xffcccc33, -205, 0xffcccc00, -13261, 0xffcccc33, 0xffcccc00, 0xffcccc33, -13261, 
            0xff99cc33, 0xffcccc00, 0xffcc9933, 0xff999900, 0xff996633, 0xff999900, 0xff666600, 0xff996633, 0xff666600, 0xff666600, 
            0xff666633, 0xff666600, 0xff666600, 0xff663300, 0xff336633, 0xff333300, 0xff333300, 0, 0, 0, 
            0, 0, 0, 0, 0xff663300, 0xff999900, 0xff999900, 0xff999933, -205, 0xffcccc00, 
            0xffcc9933, 0xffcccc33, 0xffcc9900, -13261, -205, -13261, -256, 0xffcccc33, -13261, 0xffcccc00, 
            0xffcccc33, 0xffcc9933, 0xff999900, 0xff999933, 0xff999900, 0xff996633, 0xff999900, 0xff666600, 0xff999933, 0xff666600, 
            0xff666600, 0xff663300, 0xff666600, 0xff333333, 0xff333300, 0xff666600, 0xff333300, 0xff333300, 0, 0, 
            0, 0, 0, 0, 0xff669900, 0xff999933, 0xff999933, 0xffcccc00, -13261, 0xffcccc33, 
            0xffcccc00, -13261, -205, -256, -13261, -205, -205, -13261, -205, -205, 
            -13312, -205, 0xffcccc33, 0xffcc9900, 0xff999900, 0xff666600, 0xff999933, 0xff996600, 0xff999900, 0xff666633, 
            0xff666600, 0xff666600, 0xff333333, 0xff666600, 0xff666600, 0xff333300, 0xff333300, 0xff333300, 0, 0, 
            0, 0, 0, 0xff666600, 0xff999933, 0xff999900, 0xffcc9900, 0xffcc9933, 0xffcccc00, 0xffcccc33, 
            0xffcccc00, -13261, -256, -13261, -205, -205, -205, -205, -205, -13312, 
            0xffcccc33, 0xffcccc33, 0xffcccc00, 0xff999933, 0xff666600, 0xff996633, 0xff666600, 0xff669900, 0xff666633, 0xff996600, 
            0xff666600, 0xff666633, 0xff666600, 0xff663300, 0xff333300, 0xff333300, 0xff333300, 0xff333333, 0xff333300, 0, 
            0, 0, 0, 0xff666600, 0xff999900, 0xff999933, 0xff999900, 0xffcccc33, 0xffcccc33, 0xffcccc00, 
            -13261, 0xffccff33, -13261, -205, -205, -205, -13312, 0xffcccc33, 0xffcccc00, 0xffcccc33, 
            0xffcccc33, 0xff999900, 0xff999933, 0xff996600, 0xff999900, 0xff996600, 0xff999900, 0xff666633, 0xff666600, 0xff666600, 
            0xff996633, 0xff666600, 0xff666600, 0xff333300, 0xff666600, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 0, 
            0, 0, 0xff333300, 0xff999933, 0xff999900, 0xffcc9933, 0xffcc9900, 0xffcccc33, 0xffcccc00, 0xffcccc33, 
            -13261, 0xffcccc00, -205, -205, -205, -205, -205, -205, -13261, 0xffcccc00, 
            0xffcc9933, 0xffcccc00, 0xff999900, 0xff996633, 0xff666600, 0xff669933, 0xff666600, 0xff996600, 0xff666600, 0xff666600, 
            0xff666600, 0xff666600, 0xff663300, 0xff336633, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0, 
            0, 0, 0xff333300, 0xff999900, 0xff999933, 0xff999900, 0xff999933, 0xffcccc00, -13261, -13261, 
            -205, -13261, -205, -13261, -256, -13261, 0xffcccc33, 0xffcccc00, 0xffcccc33, 0xffcc9933, 
            0xffcccc00, 0xff999933, 0xffcc9933, 0xff999900, 0xff999900, 0xff996600, 0xff999933, 0xff666600, 0xff666633, 0xff666600, 
            0xff666633, 0xff663300, 0xff336600, 0xff663300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0, 
            0, 0, 0xff666600, 0xff999933, 0xff999900, 0xffcc9933, 0xffcccc00, 0xffcccc33, -205, -205, 
            0xffcccc00, 0xffcccc33, -205, 0xffccff00, 0xffcc9933, -205, -205, 0xffcc9933, 0xff999900, 0xffcc9900, 
            0xff999933, 0xffcc9900, 0xff999900, 0xff666600, 0xff996633, 0xff669900, 0xff996600, 0xff666633, 0xff666600, 0xff663300, 
            0xff666600, 0xff666600, 0xff333333, 0xff333300, 0xff666600, 0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff330000, 
            0, 0, 0xff333300, 0xff999900, 0xff999933, 0xff999900, 0xffcccc33, 0xffcc9900, 0xff999933, 0xff999900, 
            0xffcc9933, 0xffcccc00, 0xffcccc33, -13261, 0xffcccc00, 0xffcc9933, -256, 0xffcccc33, 0xffcc9900, 0xff999933, 
            0xff999900, 0xff999933, 0xff996600, 0xff999933, 0xff669900, 0xff996633, 0xff666600, 0xff996600, 0xff666600, 0xff666633, 
            0xff336600, 0xff663300, 0xff336600, 0xff663300, 0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 0xff333333, 
            0, 0, 0xff666600, 0xffcccc33, 0xffcc9900, 0xff999933, 0xff999900, 0xff999933, 0xff999900, 0xffcc9933, 
            0xffcccc00, 0xffcccc33, 0xffcc9900, 0xffcccc00, 0xffcc9933, 0xff99cc00, 0xffcc9933, 0xffcccc00, 0xffcccc33, 0xffcccc00, 
            0xff996633, 0xff999900, 0xff999933, 0xff996600, 0xff666600, 0xff666600, 0xff666633, 0xff666600, 0xff666600, 0xff336600, 
            0xff663300, 0xff336633, 0xff663300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff663300, 0xff333300, 
            0, 0, 0xff336600, 0xff996600, 0xff999933, 0xff999900, 0xffcc9933, 0xffcccc00, 0xffcccc33, 0xffcccc00, 
            0xffcccc33, 0xffcc9900, 0xff99cc33, 0xffcc9933, 0xffcccc00, 0xffcc9933, 0xff999900, 0xff999933, 0xff999900, 0xff996633, 
            0xff669900, 0xff999900, 0xff999900, 0xff666633, 0xff996600, 0xff666633, 0xff666600, 0xff666600, 0xff663333, 0xff666600, 
            0xff333300, 0xff663300, 0xff333300, 0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 0xff333333, 0xff333300, 
            0, 0, 0xff333300, 0xff999933, 0xff999900, 0xff996600, 0xff99cc33, 0xffcc9900, 0xffcc9933, 0xff999900, 
            0xff999933, 0xffcccc33, 0xffcc9900, -13261, 0xffcccc33, 0xff999900, 0xffcc9933, 0xff999900, 0xff999933, 0xff999900, 
            0xff996600, 0xff666633, 0xff996600, 0xff666600, 0xff666600, 0xff666600, 0xff666600, 0xff666633, 0xff666600, 0xff333300, 
            0xff666600, 0xff333300, 0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 
            0, 0, 0xff663300, 0xff999900, 0xff666633, 0xff999900, 0xff999900, 0xff999933, 0xff999900, 0xff999933, 
            0xffcc9900, 0xffcccc00, 0xffcccc33, 0xffcccc33, 0xff999900, 0xff999933, 0xff999900, 0xff996600, 0xff999900, 0xff999933, 
            0xff999900, 0xff666600, 0xff666633, 0xff666600, 0xff666633, 0xff666600, 0xff996633, 0xff666600, 0xff333300, 0xff666600, 
            0xff333333, 0xff333300, 0xff333300, 0xff336600, 0xff333300, 0xff333300, 0xff333300, 0xff663333, 0xff333300, 0xff333300, 
            0, 0, 0xff333300, 0xff999933, 0xffcc9900, 0xff999933, 0xff996600, 0xff999900, 0xffcc9933, 0xffcccc00, 
            0xff999933, 0xff999933, 0xff999900, 0xffcc9900, 0xff999933, 0xff999900, 0xffcc9933, 0xff669900, 0xff996633, 0xff996600, 
            0xff666633, 0xff996600, 0xff999900, 0xff666633, 0xff666600, 0xff996600, 0xff666600, 0xff333300, 0xff666600, 0xff333300, 
            0xff333300, 0xff663300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff663333, 0xff333300, 0xff333300, 0, 
            0, 0, 0xff333300, 0xff669900, 0xff999933, 0xff999900, 0xff666600, 0xff999933, 0xff999900, 0xff999933, 
            0xffcc9900, 0xff999900, 0xff996600, 0xff999933, 0xff999900, 0xff996600, 0xff669900, 0xff996633, 0xff669900, 0xff996600, 
            0xff999900, 0xff666633, 0xff666600, 0xff666600, 0xff996600, 0xff666633, 0xff336600, 0xff663300, 0xff333333, 0xff666600, 
            0xff333300, 0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff663333, 0xff333300, 0xff333300, 0xff333300, 0, 
            0, 0, 0, 0xff663300, 0xff666600, 0xff666600, 0xff999933, 0xff999900, 0xff999933, 0xff999900, 
            0xff996600, 0xff666633, 0xff669900, 0xff996600, 0xff999933, 0xff666600, 0xff996633, 0xff999900, 0xff999933, 0xff666600, 
            0xff666633, 0xff666600, 0xff666600, 0xff996633, 0xff666600, 0xff336600, 0xff663300, 0xff336633, 0xff663300, 0xff333300, 
            0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0, 
            0, 0, 0, 0xff336600, 0xff996633, 0xff999900, 0xff666600, 0xff999933, 0xff996600, 0xff666600, 
            0xff669933, 0xff999900, 0xff996633, 0xff999900, 0xff666600, 0xff999933, 0xff996600, 0xff669900, 0xff996600, 0xff666600, 
            0xff666600, 0xff666600, 0xff666633, 0xff666600, 0xff333300, 0xff666600, 0xff333333, 0xff663300, 0xff336600, 0xff333300, 
            0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0, 
            0, 0, 0, 0xff333300, 0xff996600, 0xff666633, 0xff999900, 0xff996600, 0xff999933, 0xff999900, 
            0xff666600, 0xff996633, 0xff999900, 0xff666633, 0xff666600, 0xff996600, 0xff666600, 0xff666633, 0xff666600, 0xff666633, 
            0xff666600, 0xff666600, 0xff663300, 0xff666600, 0xff666600, 0xff333333, 0xff663300, 0xff336600, 0xff333300, 0xff333300, 
            0xff333333, 0xff333300, 0xff333300, 0xff333300, 0xff663333, 0xff333300, 0xff333300, 0xff333333, 0xff333300, 0, 
            0, 0, 0, 0, 0xff333300, 0xff999900, 0xff666633, 0xff999900, 0xff666600, 0xff996633, 
            0xff666600, 0xff666600, 0xff999900, 0xff666600, 0xff996633, 0xff669900, 0xff666633, 0xff666600, 0xff666600, 0xff663300, 
            0xff666600, 0xff666633, 0xff666600, 0xff333300, 0xff333333, 0xff663300, 0xff336600, 0xff333300, 0xff333300, 0xff333300, 
            0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0, 0, 
            0, 0, 0, 0, 0xff003300, 0xff663300, 0xff666600, 0xff666633, 0xff666600, 0xff666600, 
            0xff666600, 0xff996633, 0xff666600, 0xff666633, 0xff666600, 0xff666600, 0xff666600, 0xff663300, 0xff666633, 0xff666600, 
            0xff333300, 0xff666600, 0xff333300, 0xff666633, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333333, 
            0xff333300, 0xff333300, 0xff663300, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 0, 0, 0, 
            0, 0, 0, 0, 0xff330000, 0xff003300, 0xff666600, 0xff663300, 0xff666600, 0xff666633, 
            0xff666600, 0xff666600, 0xff666633, 0xff666600, 0xff666600, 0xff666600, 0xff666633, 0xff666600, 0xff663300, 0xff336600, 
            0xff663333, 0xff336600, 0xff663300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333333, 0xff333300, 
            0xff663300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0xff663300, 0xff666633, 0xff666600, 0xff666600, 
            0xff666600, 0xff666633, 0xff663300, 0xff666600, 0xff666600, 0xff663333, 0xff666600, 0xff663300, 0xff336600, 0xff333300, 
            0xff666600, 0xff333300, 0xff333300, 0xff333300, 0xff663300, 0xff333300, 0xff333300, 0xff333333, 0xff663300, 0xff333300, 
            0xff333300, 0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0xff333300, 0xff333300, 0xff336600, 0xff666633, 
            0xff663300, 0xff666600, 0xff336600, 0xff666600, 0xff663333, 0xff336600, 0xff663300, 0xff336600, 0xff333300, 0xff663333, 
            0xff333300, 0xff333300, 0xff663300, 0xff333333, 0xff333300, 0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 
            0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0xff333300, 0xff333300, 0xff663300, 
            0xff336600, 0xff663300, 0xff666600, 0xff333333, 0xff666600, 0xff333300, 0xff333300, 0xff333300, 0xff663333, 0xff333300, 
            0xff333300, 0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 
            0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0xff330000, 0xff333300, 
            0xff333300, 0xff666633, 0xff333300, 0xff663300, 0xff333300, 0xff333300, 0xff666600, 0xff333300, 0xff333300, 0xff333300, 
            0xff336600, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff336600, 0xff333300, 0xff333300, 
            0xff333333, 0xff333300, 0xff333300, 0xff333300, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff336600, 0xff333300, 0xff333333, 0xff333300, 0xff333300, 
            0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 0xff333333, 0xff336600, 0xff330000, 0xff663333, 0xff333300, 
            0xff333300, 0xff333300, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0xff330000, 0xff333300, 0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 
            0xff333300, 0xff663300, 0xff333300, 0xff333300, 0xff333300, 0xff333300, 0xff663300, 0xff333300, 0xff333300, 0xff333300, 
            0xff333300, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0xff333300, 0xff003300, 0xff333300, 0xff333300, 0xff663300, 0xff333300, 0xff333300, 0xff333333, 
            0xff663300, 0xff333300, 0xff333300, 0xff333300, 0xff333333, 0xff333300, 0xff333300, 0xff333333, 0xff333300, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0xff330000, 0xff333300, 0, 0xff333300, 0xff330033, 0xff333300, 
            0xff333300, 0xff333300, 0xff333300, 0xff333333, 0xff333300, 0xff330000, 0xff003300, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0xff333300, 
            0xff003300, 0xff330000, 0xff003300, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        MemoryImageSource mis = new MemoryImageSource(40, 40, pw1array, 0, 40);
        Image im = createImage(mis);
        return im;
    }

public fivestar()
    {
        lastPosition = new Point(0, 0);
        gameOver = false;
      
    }


    private Image boardimage;
    private Image offscreen;
    private Image pieceimage;
    private Point piecekeeper[];
    private static final int NOPIECE = 10;
    private int movingPiece;
    private Point lastPosition;
    private Point endpointarray[];
    private Point homearray[];
    private boolean movechecker[];
    private Point linearitycheck[];
    private int thecount;
    private boolean gameOver;
    private static final Point OUT1 = new Point(200, 59);
    private static final Point OUT2 = new Point(348, 167);
    private static final Point OUT3 = new Point(292, 341);
    private static final Point OUT4 = new Point(108, 341);
    private static final Point OUT5 = new Point(52, 167);
    private static final Point IN1 = new Point(235, 166);
    private static final Point IN2 = new Point(257, 234);
    private static final Point IN3 = new Point(200, 275);
    private static final Point IN4 = new Point(143, 234);
    private static final Point IN5 = new Point(165, 166);
    private static final Point START0 = new Point(137, 73);
    private static final Point START1 = new Point(263, 73);
    private static final Point START2 = new Point(84, 111);
    private static final Point START3 = new Point(316, 111);
    private static final Point START4 = new Point(45, 231);
    private static final Point START5 = new Point(355, 231);
    private static final Point START6 = new Point(65, 293);
    private static final Point START7 = new Point(335, 293);
    private static final Point START8 = new Point(200, 371);
    private static final int PRADIUS = 20;

}
