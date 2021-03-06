package touhou.players;

import bases.GameObject;
import bases.Vector2D;
import bases.physics.BoxCollider;
import bases.physics.PhysicsBody;
import bases.pools.GameObjectPool;
import javafx.scene.shape.Sphere;
import tklibs.SpriteUtils;
import bases.Constraints;
import bases.FrameCounter;
import bases.renderers.ImageRenderer;
import touhou.inputs.InputManager;
import touhou.players.spheres.PlayerSphere;
import touhou.scenes.GameOverScene;
import touhou.scenes.SceneManager;

import java.util.Vector;

/**
 * Created by huynq on 8/2/17.
 */
public class Player extends GameObject implements PhysicsBody {
    private static final int SPEED = 5;

    private InputManager inputManager;
    private Constraints constraints;

    private FrameCounter coolDownCounter;
    private boolean spellLock;

    private Vector2D velocity;
    private PlayerAnimator animator;

    private BoxCollider boxCollider;

    // Read - only
    private static Player instance;
    private int hp;

    private boolean immune;

    public static Player getInstance() {
        return instance;
    }

    public Player() {
        super();
        this.spellLock = false;

        this.animator = new PlayerAnimator();
        this.renderer = animator;

        this.coolDownCounter = new FrameCounter(1);
        this.velocity = new Vector2D();

        this.boxCollider = new BoxCollider(10, 10);
        this.children.add(boxCollider);

        this.hp = 2;

        addSpheres();

        // Update instance to the newest
        instance = this;
    }

    private void addSpheres() {
        PlayerSphere leftSphere = new PlayerSphere();
        leftSphere.getPosition().set(-20, 0);

        PlayerSphere rightSphere = new PlayerSphere();
        rightSphere.getPosition().set(20, 0);
        rightSphere.setReverse(true);

        this.children.add(leftSphere);
        this.children.add(rightSphere);
    }

    public void setContraints(Constraints contraints) {
        this.constraints = contraints;
    }

    public void run(Vector2D parentPostion) {
        super.run(parentPostion);

        velocity.set(0, 0);

        if (inputManager.upPressed)
            velocity.y -= SPEED;
        if (inputManager.downPressed)
            velocity.y += SPEED;
        if (inputManager.leftPressed)
            velocity.x -= SPEED;
        if (inputManager.rightPressed)
            velocity.x += SPEED;

        if (constraints != null) {
            constraints.make(position);
        }

        position.addUp(velocity);
        animator.update(this);
        castSpell();
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    private void castSpell() {
        if (inputManager.xPressed && !spellLock) {
            PlayerSpell newSpell = GameObjectPool.recycle(PlayerSpell.class);
            newSpell.getPosition().set(this.position.add(0, -30));

            spellLock = true;
            coolDownCounter.reset();
        }

        unlockSpell();
    }

    private void unlockSpell() {
        if (spellLock) {
            if (coolDownCounter.run()) {
                spellLock = false;
            }
        }
    }

    public void setInputManager(InputManager inputManager) {
        this.inputManager = inputManager;
    }


    @Override
    public BoxCollider getBoxCollider() {
        return this.boxCollider;
    }

    // non-abstract function
    public void getHit(int damage) {
        this.hp -= damage;
        if (hp <= 0) {
            SceneManager.changeScene(new GameOverScene());
        }
    }
}
