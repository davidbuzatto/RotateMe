package rotateme;

import br.com.davidbuzatto.jsge.core.Engine;
import br.com.davidbuzatto.jsge.geom.Rectangle;
import java.awt.Color;

/**
 * An obstacle!
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Obstacle {
    
    public Rectangle rect;
    public Color color;
    
    public Obstacle( Rectangle rect, Color color ) {
        this.rect = rect;
        this.color = color;
    }
    
    public void draw( Engine engine ) {
        engine.fillRectangle( rect, color );
        engine.drawRectangle( rect, Engine.BLACK );
    }
    
}
