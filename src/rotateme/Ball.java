package rotateme;

import br.com.davidbuzatto.jsge.core.Engine;
import br.com.davidbuzatto.jsge.geom.Circle;
import br.com.davidbuzatto.jsge.geom.Rectangle;
import br.com.davidbuzatto.jsge.geom.Vector2;
import br.com.davidbuzatto.jsge.utils.CollisionUtils;
import java.awt.Color;
import java.util.List;

/**
 * A ball (again...) :D
 *
 * @author Prof. Dr. David Buzatto
 */
public class Ball {

    public Vector2 pos;
    public Vector2 prevPos;
    public double radius;

    public Vector2 vel;
    public double friction;
    public double elasticity;
    public double maxFallSpeed;

    public Color color;

    public boolean dragging;
    public Vector2 pressOffset;
    
    public Circle cpUp;
    public Circle cpDown;
    public Circle cpLeft;
    public Circle cpRight;
    
    public static enum CollisionType {
        LEFT, RIGHT, UP, DOWN, NONE;
    }

    public Ball( Vector2 pos, double radius, Vector2 vel, double friction, double elasticity, double maxFallSpeed, Color color ) {
        
        this.pos = pos;
        this.prevPos = new Vector2();
        this.radius = radius;
        this.vel = vel;
        this.friction = friction;
        this.elasticity = elasticity;
        this.maxFallSpeed = maxFallSpeed;
        this.color = color;
        this.dragging = false;
        this.pressOffset = new Vector2();
        
        cpUp = new Circle( 0, 0, 5 );
        cpDown = new Circle( 0, 0, 5 );
        cpLeft = new Circle( 0, 0, 5 );
        cpRight = new Circle( 0, 0, 5 );
        
    }

    public void processInput( Vector2 mousePos, Engine engine ) {

        if ( engine.isMouseButtonPressed( Engine.MOUSE_BUTTON_LEFT ) ) {
            if ( CollisionUtils.checkCollisionPointCircle( mousePos, pos, radius ) ) {
                dragging = true;
                pressOffset.x = mousePos.x - pos.x;
                pressOffset.y = mousePos.y - pos.y;
            }
        }

        if ( engine.isMouseButtonReleased( Engine.MOUSE_BUTTON_LEFT ) ) {
            dragging = false;
        }

    }

    public void update( double delta, Rectangle limits, Vector2 mousePos, Engine engine ) {

        if ( !dragging ) {

            pos.x += vel.x * delta;
            pos.y += vel.y * delta;

            if ( pos.x - radius <= limits.x ) {
                pos.x = limits.x + radius;
                vel.x = -vel.x * elasticity;
            } else if ( pos.x + radius >= limits.x + limits.width ) {
                pos.x = limits.x + limits.width - radius;
                vel.x = -vel.x * elasticity;
            }

            if ( pos.y - radius <= limits.y ) {
                pos.y = limits.y + radius;
                vel.y = -vel.y * elasticity;
            } else if ( pos.y + radius >= limits.y + limits.height ) {
                pos.y = limits.y + limits.height - radius;
                vel.y = -vel.y * elasticity;
            }
            
            vel.x = vel.x * friction;
            vel.y = vel.y * friction;

            if ( vel.x > maxFallSpeed ) {
                vel.x = maxFallSpeed;
            } else if ( vel.x < -maxFallSpeed ) {
                vel.x = -maxFallSpeed;
            }
            
            if ( vel.y > maxFallSpeed ) {
                vel.y = maxFallSpeed;
            } else if ( vel.y < -maxFallSpeed ) {
                vel.y = -maxFallSpeed;
            }
            
        } else {
            
            pos.x = mousePos.x - pressOffset.x;
            pos.y = mousePos.y - pressOffset.y;
            
            vel.x = ( pos.x - prevPos.x ) / delta;
            vel.y = ( pos.y - prevPos.y ) / delta;
            
        }
        
        prevPos.x = pos.x;
        prevPos.y = pos.y;
        
        updateCollisionProbes();

    }
    
    public void updateCollisionProbes() {
        
        cpUp.x = pos.x + Math.cos( Math.toRadians( -90 ) ) * radius;
        cpUp.y = pos.y + Math.sin( Math.toRadians( -90 ) ) * radius + cpUp.radius;
        
        cpDown.x = pos.x + Math.cos( Math.toRadians( 90 ) ) * radius;
        cpDown.y = pos.y + Math.sin( Math.toRadians( 90 ) ) * radius - cpDown.radius;
        
        cpLeft.x = pos.x + Math.cos( Math.toRadians( 180 ) ) * radius + cpLeft.radius;;
        cpLeft.y = pos.y + Math.sin( Math.toRadians( 180 ) ) * radius;
        
        cpRight.x = pos.x + Math.cos( Math.toRadians( 0 ) ) * radius - cpRight.radius;;
        cpRight.y = pos.y + Math.sin( Math.toRadians( 0 ) ) * radius;
        
    }
    
    public void applyGravityUsingWorldRotation( double angle ) {
        
        vel.x += Math.cos( Math.toRadians( angle - 90 ) ) * Main.GRAVITY;
        vel.y += Math.sin( Math.toRadians( angle + 90 ) ) * Main.GRAVITY;
        
    }
    
    public CollisionType checkCollision( Obstacle obstacle ) {
        
        if ( CollisionUtils.checkCollisionCircleRectangle( cpDown, obstacle.rect ) ) {
            return CollisionType.DOWN;
        }
        
        if ( CollisionUtils.checkCollisionCircleRectangle( cpUp, obstacle.rect ) ) {
            return CollisionType.UP;
        }
        
        if ( CollisionUtils.checkCollisionCircleRectangle( cpLeft, obstacle.rect ) ) {
            return CollisionType.LEFT;
        }
        
        if ( CollisionUtils.checkCollisionCircleRectangle( cpRight, obstacle.rect ) ) {
            return CollisionType.RIGHT;
        }
        
        return CollisionType.NONE;
        
    }
    
    public void resolveCollisionObstacles( List<Obstacle> obstacles ) {
        
        for ( Obstacle o : obstacles ) {
            switch ( checkCollision( o ) ) {
                case UP:
                    pos.y = o.rect.y + o.rect.height + radius;
                    vel.y = -vel.y;
                    break;
                case DOWN:
                    pos.y = o.rect.y - radius;
                    vel.y = -vel.y;
                    break;
                case LEFT:
                    pos.x = o.rect.x + o.rect.width + radius;
                    vel.x = -vel.x;
                    break;
                case RIGHT:
                    pos.x = o.rect.x - radius;
                    vel.x = -vel.x;
                    break;
            }
            updateCollisionProbes();
        }
        
    }
    
    public void resolveCollisionBalls( List<Ball> balls ) {
        
        for ( Ball ball : balls ) {
            if ( ball != this ) {
                if ( CollisionUtils.checkCollisionCircles( pos, radius, ball.pos, ball.radius ) ) {
                    
                    double angle = Math.atan2( pos.y - ball.pos.y, pos.x - ball.pos.x );
                    vel.x += Math.cos( angle ) * 30;
                    vel.y += Math.sin( angle ) * 30;
                    
                    ball.vel.x += Math.cos( angle + Math.PI ) * 30;
                    ball.vel.y += Math.sin( angle + Math.PI ) * 30;
                    
                }
            }
        }
        
    }
    
    public void draw( Engine engine ) {
        
        engine.fillCircle( pos, radius, color );
        engine.drawCircle( pos, radius, Engine.BLACK );
        
        /*engine.fillCircle( cpUp, Engine.RED );
        engine.fillCircle( cpDown, Engine.GREEN );
        engine.fillCircle( cpLeft, Engine.ORANGE );
        engine.fillCircle( cpRight, Engine.MAGENTA );*/
        
    }

}
