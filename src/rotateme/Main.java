package rotateme;

import br.com.davidbuzatto.jsge.core.Camera2D;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.geom.Rectangle;
import br.com.davidbuzatto.jsge.math.MathUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * JSGE basic game template.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Main extends EngineFrame {
    
    public static double GRAVITY = 50;
    
    //private Ball ball;
    private List<Ball> balls;
    private List<Obstacle> obstacles;
    
    private Rectangle worldLimits;
    private Rectangle boxLimits;
    
    private Camera2D camera;
    
    public Main() {
        super( 800, 600, "RotateMe!", 60, true );
    }
    
    /**
     * Creates the game world.
     * 
     * This method runs just one time during engine initialization.
     */
    @Override
    public void create() {
        
        int worldWidth = 2700;
        int worldHeight = 2700;
        
        balls = new CopyOnWriteArrayList<>();
        /*balls.add( new Ball( 
            new Vector2( worldWidth / 2, worldHeight / 2 ), 
            30, 
            new Vector2( 200, 200 ), 
            0.99, 0.9, 1000,
            BLUE
        ));*/
        
        obstacles = new ArrayList<>();
        createObstacles();
        
        int boundaryWidth = 30;
        worldLimits = new Rectangle( 0, 0, worldWidth, worldHeight );
        boxLimits = new Rectangle( boundaryWidth, boundaryWidth, worldLimits.width - boundaryWidth * 2, worldLimits.height - boundaryWidth * 2 );
        
        camera = new Camera2D( 
            new Vector2(), 
            new Vector2( getScreenWidth() / 2, getScreenHeight() / 2 ),
            0.0,
            1.0
        );
        
    }

    /**
     * Reads user input and update game world.
     * 
     * Input methods should be used here.
     * You MUST NOT use any of the engine drawing methods here.
     */
    @Override
    public void update() {
        
        double delta = getFrameTime();
        Vector2 mousePos = camera.getScreenToWorld( getMouseX(), getMouseY() );
        
        for ( Ball ball : balls ) {
            ball.processInput( mousePos, this );
            ball.update( delta, boxLimits, mousePos, this );
            ball.resolveCollisionBalls( balls );
            ball.resolveCollisionObstacles( obstacles );
        }
        
        if ( isKeyDown( KEY_RIGHT ) ) {
            camera.rotation += 2.0;
        } else if ( isKeyDown( KEY_LEFT ) ) {
            camera.rotation -= 2.0;
        }
        
        if ( isKeyDown( KEY_UP ) ) {
            camera.zoom += 0.01;
        } else if ( isKeyDown( KEY_DOWN ) ) {
            camera.zoom -= 0.01;
            if ( camera.zoom < 0.1 ) {
                camera.zoom = 0.1;
            }
        }
        
        if ( isKeyPressed( KEY_R ) ) {
            camera.rotation = 0.0;
            camera.zoom = 1.0;
        }
        
        if ( isMouseButtonDown( MOUSE_BUTTON_LEFT ) ) {
            balls.add( new Ball(
                new Vector2( mousePos.x, mousePos.y ), 
                30, 
                new Vector2( MathUtils.getRandomValue( -200, 200 ), 200 ), 
                0.99, 0.9, 1000,
                ColorUtils.colorFromHSV( MathUtils.getRandomValue( 180, 230 ), 1, MathUtils.clamp( MathUtils.getRandomValue( 80, 101 ) / 100.0, 0.8, 1.0 ) )
            ));
        }
        
        if ( isMouseButtonPressed( MOUSE_BUTTON_RIGHT ) ) {
            obstacles.add( new Obstacle(
                new Rectangle( mousePos.x - 30, mousePos.y - 30, 60, 60 ),
                GREEN
            ));
        }
        
        for ( Ball ball : balls ) {
            ball.applyGravityUsingWorldRotation( camera.rotation );
        }
        updateCamera();
        
    }
    
    /**
     * Draws the game world.
     * 
     * All drawing related operations MUST be performed here.
     */
    @Override
    public void draw() {
        
        clearBackground( WHITE );
        
        beginMode2D( camera );
        
        fillRectangle( worldLimits, DARKGRAY );
        drawChecked( (int) boxLimits.x, (int) boxLimits.y, 30, (int) boxLimits.width / 30, (int) boxLimits.height / 30 );
        
        for ( Obstacle o : obstacles ) {
            o.draw( this );
        }
        for ( Ball ball : balls ) {
            ball.draw( this );
        }
        
        endMode2D();
        
        drawFPS( 10, 20 );
    
    }
    
    private void drawChecked( int startX, int startY, int width, int hQuantity, int vQuantity ) {
        
        for ( int i = 0; i < vQuantity; i++ ) {
            for ( int j = 0; j < hQuantity; j++ ) {
                if ( i % 2 == 0 ) {
                    if ( j % 2 == 0 ) {
                        fillRectangle( startX + j * width, startY + i * width, width, width, WHITE );
                    } else {
                        fillRectangle( startX + j * width, startY + i * width, width, width, LIGHTGRAY );
                    }
                } else {
                    if ( j % 2 == 0 ) {
                        fillRectangle( startX + j * width, startY + i * width, width, width, LIGHTGRAY );
                    } else {
                        fillRectangle( startX + j * width, startY + i * width, width, width, WHITE );
                    }
                }
            }
        }
        
    }
    
    private void createObstacles() {
        
        int startX = 400;
        int startY = 400;
        int width = 50;
        int distance = 100;
        
        for ( int i = 0; i < 10; i++ ) {
            for ( int j = 0; j < 10; j++ ) {
                obstacles.add( new Obstacle(
                    new Rectangle( 
                        startX + j * ( width + distance ) + ( i % 2 == 0 ? 0 : width + distance / 2 - width / 2 ), 
                        startY + i * ( width + distance ), 
                        width, width
                    ),
                    GRAY
                ));
            }
        }
        
    }
    
    private void updateCamera() {
        
        /*if ( ball.pos.x <= getScreenWidth() / 2 ) {
            camera.target.x = getScreenWidth() / 2;
        } else if ( ball.pos.x >= worldLimits.width - getScreenWidth() / 2 ) {
            camera.target.x = worldLimits.width - getScreenWidth() / 2 ;
        } else {
            camera.target.x = ball.pos.x;
        }
        
        if ( ball.pos.y <= getScreenHeight() / 2 ) {
            camera.target.y = getScreenHeight()/ 2;
        } else if ( ball.pos.y >= worldLimits.height - getScreenHeight()/ 2 ) {
            camera.target.y = worldLimits.height - getScreenHeight()/ 2 ;
        } else {
            camera.target.y = ball.pos.y;
        }*/
        
        camera.target.x = worldLimits.width / 2;
        camera.target.y = worldLimits.height / 2;
        
    }
    
    public static void main( String[] args ) {
        new Main();
    }
    
}
