import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Renders the Android TV banner (320x180 dp) at several densities. */
public class Banner {
    public static void main(String[] args) throws Exception {
        String out = args[0];
        render(out + "/drawable-mdpi/banner.png", 1f);
        render(out + "/drawable-hdpi/banner.png", 1.5f);
        render(out + "/drawable-xhdpi/banner.png", 2f);
        render(out + "/drawable-xxhdpi/banner.png", 3f);
    }

    static void render(String file, float s) throws Exception {
        int w = Math.round(320 * s), h = Math.round(180 * s);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setColor(new Color(0x1E88E5));
        g.fillRect(0, 0, w, h);

        // Four level bars, the same glyph as the launcher icon, left of the name.
        g.setColor(Color.WHITE);
        float x0 = 36 * s, cy = 90 * s, bw = 10 * s, gap = 8 * s;
        float[] heights = {40, 88, 56, 72};
        for (int i = 0; i < heights.length; i++) {
            float bh = heights[i] * s;
            float x = x0 + i * (bw + gap);
            g.fill(new RoundRectangle2D.Float(x, cy - bh / 2, bw, bh, bw, bw));
        }

        Font font = new Font("SansSerif", Font.BOLD, Math.round(34 * s));
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        float tx = x0 + 4 * (bw + gap) + 14 * s;
        float baseline = cy + fm.getAscent() / 2f - fm.getDescent() / 2f;
        if (fm.stringWidth("Steady Volume") > w - tx - 16 * s) {
            g.drawString("Steady", tx, cy - 4 * s);
            g.drawString("Volume", tx, cy - 4 * s + fm.getHeight());
        } else {
            g.drawString("Steady Volume", tx, baseline);
        }
        g.dispose();
        new File(file).getParentFile().mkdirs();
        ImageIO.write(img, "png", new File(file));
    }
}
