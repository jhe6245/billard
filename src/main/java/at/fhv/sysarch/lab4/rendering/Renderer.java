package at.fhv.sysarch.lab4.rendering;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import at.fhv.sysarch.lab4.game.Cue;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;

import at.fhv.sysarch.lab4.game.Ball;
import at.fhv.sysarch.lab4.game.Table;
import at.fhv.sysarch.lab4.game.Table.TablePart;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.transform.Affine;

public class Renderer extends AnimationTimer {
    private long lastUpdate;
    private final List<Ball> balls;
    private Table table;
    private Cue cue;

    private final GraphicsContext gc;

    private final double centerX;
    private final double centerY;
    private final double sceneWidth;
    private final double sceneHeight;

    private final double scale;

    private final Affine poolCoords;
    private final Affine jfxCoords;
    private final Affine fpsTrans;

    private final double[] xsBuffer = new double[4];
    private final double[] ysBuffer = new double[4];
    
    private String strikeMessage;
    private String foulMessage;
    private String actionMessage;
    private int player1Score;
    private int player2Score;

    private Optional<FrameListener> frameListener;

    public Renderer(final GraphicsContext gc, int sceneWidth, int sceneHeight) {
        this.gc = gc;

        this.balls = new ArrayList<>();
        
        this.centerX = (double) sceneWidth * 0.5;
        this.centerY = (double) sceneHeight * 0.5;
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;

        this.scale = Math.min(sceneWidth / 1920.0, sceneHeight / 1080.0) * 400;
        
        this.frameListener = Optional.empty();
        
        this.poolCoords = new Affine();
        this.poolCoords.appendTranslation(this.centerX, this.centerY);

        this.fpsTrans = new Affine();
        this.fpsTrans.appendTranslation(0, 10);
        this.fpsTrans.appendScale(1.5, 1.5);
        
        // caching of identity for reverting to JavaFX coordinates
        this.jfxCoords = new Affine();

        this.gc.setStroke(Color.WHITE);
    }

    public void setStrikeMessage(String strikeMessage) {
        this.strikeMessage = strikeMessage;
    }

    public void setActionMessage(String actionMessage) {
        this.actionMessage = actionMessage;
    }

    public void setFoulMessage(String foulMessage) {
        this.foulMessage = foulMessage;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public void incrementPlayer1Score(int delta) {
        this.player1Score += delta;
    }

    public void incrementPlayer2Score(int delta) {
        this.player2Score += delta;
    }

    public void addBall(Ball b) {
        this.balls.add(b);
    }

    public void removeBall(Ball b) {
        this.balls.remove(b);
    }

    public void setTable(Table t) {
        this.table = t;
    }

    public void setCue(Cue c) {
        this.cue = c;
    }

    public Cue getCue() {
        return cue;
    }

    public void setFrameListener(FrameListener l) {
        this.frameListener = Optional.of(l);
    }

    public double screenToPhysicsX(double screenX) {
        // screen has origin (0/0) top left corner,
        // physics has origin (0/0) center of the screen
        // and physics is scaled by factor SCALE

        double pX = screenX - centerX;
        pX = pX / scale;

        return pX;
    }

    public double screenToPhysicsY(double screenY) {
        // screen has origin (0/0) top left corner,
        // physics has origin (0/0) center of the screen
        // and physics is scaled by factor SCALE
        
        double pY = screenY - centerY;
        pY = pY / scale;

        return pY;
    }

    @Override
    public void handle(long now) {
        double dt = (double) (now - lastUpdate) / 1_000_000_000.0;

        this.frameListener.ifPresent(l -> l.onFrame(dt));

        this.clearWithColorBackground();
        this.drawTable();
        this.drawBalls();
        this.drawCue();
        this.drawFPS(dt);
        
        this.drawMessages();
        
        this.lastUpdate = now;
    }

    private void clearWithColorBackground() {
        // clearing of rectangle happens in JavaFX coordinate system
        this.gc.setTransform(this.jfxCoords);
        this.gc.clearRect(0, 0, this.sceneWidth, this.sceneHeight);
    }

    private void drawTable() {
        // render green table surface (which is not a physical body!)
        double tableWidth = (Table.Constants.WIDTH + Table.Constants.CUSHION_SIZE) * scale;
        double tableHeight = (Table.Constants.HEIGHT + Table.Constants.CUSHION_SIZE) * scale;
        double tableX = -tableWidth * 0.5;
        double tableY = -tableHeight * 0.5;
        this.gc.setTransform(this.poolCoords);
        this.gc.setFill(Color.DARKGREEN);
        this.gc.fillRect(tableX, tableY, tableWidth, tableHeight);
       
        List<BodyFixture> fs = this.table.getBody().getFixtures();
        for (BodyFixture f : fs) {
            TablePart tp = (TablePart) f.getUserData();

            switch (tp) {
                case POCKET:
                    this.renderPocket((Circle) f.getShape());
                    break;
                case CUSHION:
                    this.renderCushion((Polygon) f.getShape());
                    break;
            }
        }
    }

    private void drawBalls() {
        this.gc.setLineWidth(1);

        // render billard balls after table, so they appear on top
        for (Ball b : this.balls) {
            if(!b.getBody().isActive())
                continue;

            Transform t = b.getBody().getTransform();
            Circle s = b.getShape();

            double r = s.getRadius() * scale;
            double d = r * 2;
            
            double x = t.getTranslationX() * scale;
            double y = t.getTranslationY() * scale;
            
            // rendering of billard balls happens in their own coordinates
            // center of the world is at center of the window not top left corner
            Affine ballTrans = new Affine(this.poolCoords);
            ballTrans.appendTranslation(x, y);
            
            this.gc.setTransform(ballTrans);

            // NOTE center of physics circle is in the center
            // but javafx draws ovals from top left corner

            this.gc.setFill(b.getColor());
            this.gc.fillOval(-r, -r, d, d);
            
            if (b.isWhite()) {
                continue;
            }

            if (!b.isSolid()) {
                this.gc.setFill(Color.WHITE);
                this.gc.fillArc(-r * 0.75, -r * 0.95, d * 0.75, r * 0.75, 0, 180, ArcType.ROUND);
                this.gc.fillArc(-r * 0.75, r * 0.2, d * 0.75, r * 0.75, 180, 180, ArcType.ROUND);
            }

            // white circle with black number is same for all balls (except white) 
            this.gc.setFill(Color.WHITE);
            this.gc.fillOval(-r * 0.5, -r * 0.5, d *  0.5, d *  0.5);

            this.gc.setStroke(Color.BLACK);
            int xOff = b.ordinal() >= 9 ? -8 : -5;
            this.gc.strokeText(b.ordinal() + 1 + "", xOff, 5);
        }
    }

    private void drawCue() {
        if(this.cue == null)
            return;

        var cueTf = this.cue.getBody().getTransform();

        this.gc.setTransform(jfxCoords);
        this.gc.translate(centerX, centerY);
        this.gc.scale(scale, scale);

        // now in physics space

        this.gc.translate(cueTf.getTranslationX(), cueTf.getTranslationY());
        this.gc.rotate(cueTf.getRotationAngle() * 180 / Math.PI);


        var rect = this.cue.getGeometry();

        double width = rect.getWidth();
        double height = rect.getHeight();

        double rotation = rect.getRotationAngle();
        Vector2 center = rect.getCenter();

        var transform1 = this.gc.getTransform();

        this.gc.translate(center.x, center.y);
        this.gc.rotate(rotation * 180 / Math.PI);

        this.gc.setFill(Color.BEIGE);
        this.gc.fillRect(-width / 2, -height / 2, width, height);

        this.gc.setFill(Color.RED);
        this.gc.fillRect(-width / 2, -height / 2, Cue.Constants.TIP_THICKNESS, height);

        if(cue.getBody().isActive()) {
            this.gc.setStroke(Color.RED);
            this.gc.setLineWidth(.005);
            this.gc.strokeRect(-width / 2, -height / 2, width, height);
        }

        this.gc.setTransform(transform1);

    }

    private void drawFPS(double dt) {
        double fps = 1.0 / dt;

        // rendering text in JavaFX coordinate system
        this.gc.setTransform(this.fpsTrans);
        this.gc.setFill(Color.BLACK);
        this.gc.fillText(String.format("%,.2f", fps) + " FPS", 0, 5);
    }

    private void drawMessages() {
        // rendering text in JavaFX coordinate system
        this.gc.setTransform(this.jfxCoords);
        this.gc.setFill(Color.BLACK);

        Affine actionMsgTrans = new Affine(this.jfxCoords);
        Affine strikeMsgTrans = new Affine(this.jfxCoords);
        Affine foulMsgTrans = new Affine(this.jfxCoords);

        Affine player1ScoreTrans = new Affine(this.jfxCoords);
        Affine player2ScoreTrans = new Affine(this.jfxCoords);

        actionMsgTrans.appendTranslation(this.centerX - 250, 200);
        actionMsgTrans.appendScale(2, 2);

        strikeMsgTrans.appendTranslation(this.centerX - 250, 150);
        strikeMsgTrans.appendScale(2, 2);

        foulMsgTrans.appendTranslation(this.centerX - 250,  this.centerY + 300);
        foulMsgTrans.appendScale(2, 2);

        player1ScoreTrans.appendTranslation(10, this.sceneHeight - 100);
        player1ScoreTrans.appendScale(5, 5);
        
        player2ScoreTrans.appendTranslation(this.centerX + 300, this.sceneHeight - 100);
        player2ScoreTrans.appendScale(5, 5);

        this.gc.setTransform(actionMsgTrans);
        this.gc.fillText(this.actionMessage, 0, 0);

        this.gc.setTransform(strikeMsgTrans);
        this.gc.fillText(this.strikeMessage, 0, 0);

        this.gc.setTransform(foulMsgTrans);
        this.gc.fillText(this.foulMessage, 0, 0);

        this.gc.setTransform(player1ScoreTrans);
        this.gc.fillText(String.format("Player 1 score: %d", this.player1Score), 0, 0);

        this.gc.setTransform(player2ScoreTrans);
        this.gc.fillText(String.format("Player 2 score: %d", this.player2Score), 0, 0);
    }

    private void renderCushion(Polygon p) {
        this.gc.setFill(Color.BROWN);
        
        Vector2[] vs = p.getVertices();
        
        int i = 0;
        for (Vector2 v : vs) {
            xsBuffer[i] = v.x * scale;
            ysBuffer[i] = v.y * scale;
            i++;
        }

        Affine cushionTrans = new Affine(this.poolCoords);
        this.gc.setTransform(cushionTrans);

        this.gc.fillPolygon(xsBuffer, ysBuffer, vs.length);
    }

    private void renderPocket(Circle c) {
        this.gc.setFill(Color.BLACK);

        double r = c.getRadius() * scale;
        double d = r * 2;
        double x = c.getCenter().x * scale;
        double y = c.getCenter().y * scale;
        
        Affine pocketTrans = new Affine(this.poolCoords);
        pocketTrans.appendTranslation(x, y);

        this.gc.setTransform(pocketTrans);

        // center of phyics circle is in the center
        // javafx draws ovals from top left corner
        this.gc.fillOval(-r, -r, d, d);
    }
}