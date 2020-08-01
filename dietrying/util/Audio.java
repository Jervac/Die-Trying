package app.dietrying.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;

public class Audio {

    public static boolean muted = false;

    public static Array<Music> songs = new Array<Music>();
    public static Array<String> songPaths = new Array<String>();

    public static Array<Sound> sfxs = new Array<Sound>();
    public static Array<String> sfxPaths = new Array<String>();

    public static void loadSong(String s) {
        songs.add(Gdx.audio.newMusic(Gdx.files.internal(s)));
        songPaths.add(s);
    }

    public static void loadEffect(String s) {
        sfxs.add(Gdx.audio.newSound(Gdx.files.internal(s)));
        sfxPaths.add(s);
    }

    public static Music getSong(String path) {
        for (Music m : songs) {
            for (String s : songPaths) {
                if (path.equals(s)) {
                    // return index of song from strings as index from songs (sounds retarded)
                    int i = songPaths.indexOf(s, true);
                    return songs.get(i);
                }
            }
        }
        //Logger.audionNOotoFOund
        return null;
    }

    public static void playSong(String s) {
        for (int i = 0; i < songPaths.size - 1; ++i) {
            if (songPaths.get(i) == s && !muted) {
                songs.get(i).play();
            }
        }
    }

    public static void playEffect(String s) {
        for (int i = 0; i < sfxPaths.size - 1; ++i) {
            if (sfxPaths.get(i) == s && !muted) {
                sfxs.get(i).play();
            }
        }

        if (!sfxPaths.contains(s, true))
            System.out.println("couldn't load " + s);
    }

    public static void playEffect(String s, float volume) {
        for (int i = 0; i < sfxPaths.size - 1; ++i) {
            if (sfxPaths.get(i) == s && !muted) {
                sfxs.get(i).play(volume);
            }
        }

        if (!sfxPaths.contains(s, true))
            System.out.println("couldn't load " + s);
    }

    public static void pauseSong(String s) {
        for (int i = 0; i < songPaths.size - 1; ++i) {
            if (songPaths.get(i) == s) {
                songs.get(i).pause();
            }
        }
    }

    public static void pauseEffect(String s) {
        for (int i = 0; i < sfxPaths.size - 1; ++i) {
            if (sfxPaths.get(i) == s) {
                sfxs.get(i).pause();
            }
        }
    }

    public static void dispose() {
        for (Music s : songs)
            s.dispose();
        songs.clear();

        songPaths.clear();

        for (Sound s : sfxs)
            s.dispose();
        sfxs.clear();

        sfxPaths.clear();
    }

}
