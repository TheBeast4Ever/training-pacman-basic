package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.Timer;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.swing.assets.AssetLoader;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManGameScenes;
import de.amr.games.pacman.ui.swing.pacman.PacManGameScenes;
import de.amr.games.pacman.ui.swing.rendering.DebugRendering;

/**
 * A Swing implementation of the Pac-Man game UI interface.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	static final int KEY_SLOW_MODE = KeyEvent.VK_S;
	static final int KEY_FAST_MODE = KeyEvent.VK_F;
	static final int KEY_DEBUG_MODE = KeyEvent.VK_D;

	private final Dimension unscaledSize_px;
	private final V2i scaledSize_px;
	private final float scaling;
	private final JFrame window;
	private final Timer titleUpdateTimer;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private final PacManGameScenes pacManGameScenes;
	private final MsPacManGameScenes msPacManGameScenes;

	private PacManGameModel game;
	private GameScene currentScene;

	private final Deque<FlashMessage> flashMessageQ = new ArrayDeque<>();

	private boolean muted;

	public PacManGameSwingUI(PacManGameController controller, double scalingFactor) {
		scaling = (float) scalingFactor;
		unscaledSize_px = new Dimension(28 * TS, 36 * TS);
		scaledSize_px = new V2f(unscaledSize_px.width, unscaledSize_px.height).scaled(this.scaling).toV2i();

		canvas = new Canvas();
		canvas.setSize(scaledSize_px.x, scaledSize_px.y);
		canvas.setFocusable(false);

		window = new JFrame();
		window.setTitle("Pac-Man");
		window.setResizable(false);
		window.setFocusable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setIconImage(AssetLoader.image("/pacman/graphics/pacman.png"));
		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				handleKeyboardInput(e);
			}
		});
		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				controller.endGame();
			}
		});
		window.getContentPane().add(canvas);

		keyboard = new Keyboard(window);

		titleUpdateTimer = new Timer(1000,
				e -> window.setTitle(String.format("Pac-Man / Ms. Pac-Man (%d fps)", clock.frequency)));
		titleUpdateTimer.start();

		pacManGameScenes = new PacManGameScenes();
		msPacManGameScenes = new MsPacManGameScenes();
		setGame(controller.getGame());

		log("Pac-Man Swing UI created");
	}

	@Override
	public void setGame(PacManGameModel newGame) {
		if (newGame instanceof PacManGame) {
			pacManGameScenes.createScenes((PacManGame) newGame, unscaledSize_px);
		} else if (newGame instanceof MsPacManGame) {
			msPacManGameScenes.createScenes((MsPacManGame) newGame, unscaledSize_px);
		} else {
			throw new IllegalArgumentException("Cannot set game, game is not supported: " + newGame);
		}
		this.game = newGame;
	}

	@Override
	public void show() {
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.requestFocus();
		canvas.createBufferStrategy(2);
		moveMousePointerOutOfSight();
	}

	@Override
	public void reset() {
		currentScene.end();
	}

	@Override
	public void showFlashMessage(String message, long ticks) {
		flashMessageQ.add(new FlashMessage(message, ticks));
	}

	@Override
	public void update() {
		GameScene newScene = null;
		if (game instanceof PacManGame) {
			newScene = pacManGameScenes.selectScene(game);
		} else if (game instanceof MsPacManGame) {
			newScene = msPacManGameScenes.selectScene(game);
		}
		if (newScene == null) {
			throw new IllegalStateException("No scene found for game state " + game.state);
		}
		if (currentScene != newScene) {
			if (currentScene != null) {
				currentScene.end();
			}
			newScene.start();
			log("Current scene changed from %s to %s", currentScene, newScene);
		}
		currentScene = newScene;
		currentScene.update();

		FlashMessage message = flashMessageQ.peek();
		if (message != null) {
			message.timer.tick();
			if (message.timer.expired()) {
				flashMessageQ.remove();
			}
		}
	}

	@Override
	public void render() {
		BufferStrategy buffers = canvas.getBufferStrategy();
		do {
			do {
				Graphics2D g = (Graphics2D) buffers.getDrawGraphics();
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				g.scale(scaling, scaling);
				currentScene.render(g);
				drawFlashMessage(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	@Override
	public Optional<PacManGameAnimation> animation() {
		if (game instanceof MsPacManGame) {
			return Optional.of(MsPacManGameScenes.rendering);
		} else {
			return Optional.of(PacManGameScenes.rendering);
		}
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			// TODO that's just a hack, should have real mute functionality
			return Optional.empty();
		}
		if (game instanceof MsPacManGame) {
			return Optional.ofNullable(MsPacManGameScenes.soundManager);
		} else {
			return Optional.ofNullable(PacManGameScenes.soundManager);
		}
	}

	@Override
	public void mute(boolean b) {
		muted = b;
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec); // TODO
		return pressed;
	}

	private void handleKeyboardInput(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KEY_SLOW_MODE: {
			clock.targetFreq = clock.targetFreq == 60 ? 30 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Slow speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
			break;
		}
		case KEY_FAST_MODE: {
			clock.targetFreq = clock.targetFreq == 60 ? 120 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Fast speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
			break;
		}
		case KEY_DEBUG_MODE:
			DebugRendering.on = !DebugRendering.on;
			log("UI debug mode is %s", DebugRendering.on ? "on" : "off");
			break;
		default:
			break;
		}
	}

	private void drawFlashMessage(Graphics2D g) {
		FlashMessage message = flashMessageQ.peek();
		if (message != null) {
			double alpha = Math.cos(Math.PI * message.timer.running() / (2 * message.timer.getDuration()));
			g.setColor(Color.BLACK);
			g.fillRect(0, unscaledSize_px.height - 16, unscaledSize_px.width, 16);
			g.setColor(new Color(1, 1, 0, (float) alpha));
			g.setFont(new Font(Font.SERIF, Font.BOLD, 10));
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(message.text, (unscaledSize_px.width - g.getFontMetrics().stringWidth(message.text)) / 2,
					unscaledSize_px.height - 3);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
	}

	private void moveMousePointerOutOfSight() {
		try {
			Robot robot = new Robot();
			robot.mouseMove(window.getX() + 10, window.getY());
		} catch (AWTException x) {
			x.printStackTrace();
		}
	}
}