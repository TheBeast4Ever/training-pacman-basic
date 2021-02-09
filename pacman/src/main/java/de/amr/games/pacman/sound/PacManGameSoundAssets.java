package de.amr.games.pacman.sound;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

public class PacManGameSoundAssets {

	private static final PacManGameSoundAssets IT = new PacManGameSoundAssets();

	private final Map<PacManGameSound, URL> pacManSounds;
	private final Map<PacManGameSound, URL> msPacManSounds;

	private URL url(String path) {
		return getClass().getResource(path);
	}

	public static URL getPacManSoundURL(PacManGameSound sound) {
		return IT.pacManSounds.get(sound);
	}

	public static URL getMsPacManSoundURL(PacManGameSound sound) {
		return IT.msPacManSounds.get(sound);
	}

	private PacManGameSoundAssets() {
		//@formatter:off
		pacManSounds = new EnumMap<>(PacManGameSound.class);
		pacManSounds.put(PacManGameSound.CREDIT,             url("/pacman/sound/credit.wav"));
		pacManSounds.put(PacManGameSound.EXTRA_LIFE,         url("/pacman/sound/extend.wav"));
		pacManSounds.put(PacManGameSound.GAME_READY,         url("/pacman/sound/game_start.wav"));
		pacManSounds.put(PacManGameSound.BONUS_EATEN,   url("/pacman/sound/eat_fruit.wav"));
		pacManSounds.put(PacManGameSound.PACMAN_MUNCH,       url("/pacman/sound/munch_1.wav"));
		pacManSounds.put(PacManGameSound.PACMAN_DEATH,       url("/pacman/sound/death_1.wav"));
		pacManSounds.put(PacManGameSound.PACMAN_POWER,       url("/pacman/sound/power_pellet.wav"));
		pacManSounds.put(PacManGameSound.GHOST_EATEN,        url("/pacman/sound/eat_ghost.wav"));
		pacManSounds.put(PacManGameSound.GHOST_EYES,         url("/pacman/sound/retreating.wav"));
		pacManSounds.put(PacManGameSound.GHOST_SIREN_1,      url("/pacman/sound/siren_1.wav"));
		pacManSounds.put(PacManGameSound.GHOST_SIREN_2,      url("/pacman/sound/siren_2.wav"));
		pacManSounds.put(PacManGameSound.GHOST_SIREN_3,      url("/pacman/sound/siren_3.wav"));
		pacManSounds.put(PacManGameSound.GHOST_SIREN_4,      url("/pacman/sound/siren_4.wav"));
		pacManSounds.put(PacManGameSound.GHOST_SIREN_5,      url("/pacman/sound/siren_5.wav"));
		pacManSounds.put(PacManGameSound.INTERMISSION_1,     url("/pacman/sound/intermission.wav"));
		pacManSounds.put(PacManGameSound.INTERMISSION_2,     url("/pacman/sound/intermission.wav"));
		pacManSounds.put(PacManGameSound.INTERMISSION_3,     url("/pacman/sound/intermission.wav"));

		msPacManSounds = new EnumMap<>(PacManGameSound.class);
		msPacManSounds.put(PacManGameSound.CREDIT,           url("/mspacman/sound/Coin Credit.wav"));
		msPacManSounds.put(PacManGameSound.EXTRA_LIFE,       url("/mspacman/sound/Extra Life.wav"));
		msPacManSounds.put(PacManGameSound.GAME_READY,       url("/mspacman/sound/Start.wav"));
		msPacManSounds.put(PacManGameSound.BONUS_EATEN, url("/mspacman/sound/Fruit.wav"));
		msPacManSounds.put(PacManGameSound.PACMAN_MUNCH,     url("/mspacman/sound/Ms. Pac Man Pill.wav"));
		msPacManSounds.put(PacManGameSound.PACMAN_DEATH,     url("/mspacman/sound/Died.wav"));
		msPacManSounds.put(PacManGameSound.PACMAN_POWER,     url("/mspacman/sound/Scared Ghost.wav"));
		msPacManSounds.put(PacManGameSound.GHOST_EATEN,      url("/mspacman/sound/Ghost.wav"));
		msPacManSounds.put(PacManGameSound.GHOST_EYES,       url("/mspacman/sound/Ghost Eyes.wav"));
		msPacManSounds.put(PacManGameSound.GHOST_SIREN_1,    url("/mspacman/sound/Ghost Noise.wav"));
		msPacManSounds.put(PacManGameSound.GHOST_SIREN_2,    url("/mspacman/sound/Ghost Noise 1.wav"));
		msPacManSounds.put(PacManGameSound.GHOST_SIREN_3,    url("/mspacman/sound/Ghost Noise 2.wav"));
		msPacManSounds.put(PacManGameSound.GHOST_SIREN_4,    url("/mspacman/sound/Ghost Noise 3.wav"));
		msPacManSounds.put(PacManGameSound.GHOST_SIREN_5,    url("/mspacman/sound/Ghost Noise 4.wav"));
		msPacManSounds.put(PacManGameSound.INTERMISSION_1,   url("/mspacman/sound/They Meet Act 1.wav"));
		msPacManSounds.put(PacManGameSound.INTERMISSION_2,   url("/mspacman/sound/The Chase Act 2.wav"));
		msPacManSounds.put(PacManGameSound.INTERMISSION_3,   url("/mspacman/sound/Junior Act 3.wav"));
		//@formatter:on
	}
}