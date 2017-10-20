package com.gmail.jorgegilcavazos.ballislife.util;

import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RedditUtilsTest {

    @Test
    public void testParseNbaFlair(){
        String actual = RedditUtils
                .parseNbaFlair("Flair {cssClass='Celtics1', text='The Truth'}");
        String expected = "The Truth";

        assertEquals(expected, actual);
    }

    @Test
    public void testParseNbaFlair_Empty(){
        String actual = RedditUtils.parseNbaFlair(null);
        String expected = "";

        assertEquals(expected, actual);
    }

    @Test
    public void testParseNbaFlair_Invalid(){
        String actual = RedditUtils.parseNbaFlair("Flair {cssClass='Spurs'}");
        String expected = "";

        assertEquals(expected, actual);
    }

    @Test
    public void testFindGameThreadId() {
        // Create fake threads with different titles.
        List<GameThreadSummary> gameThreadList = new ArrayList<>();
        gameThreadList.add(makeFakeGameThreadSummary("id0", "Post Game Thread: Los Angeles Clippers @ Hawks", 0));
        gameThreadList.add(makeFakeGameThreadSummary("id1", "[Post Game Thread] Los Angeles Lakers @ San Antonio Spurs", 0));
        gameThreadList.add(makeFakeGameThreadSummary("id2", "Game Thread: Los Angeles Clippers @ Hawks", 0));
        gameThreadList.add(makeFakeGameThreadSummary("id3", "Game Thread: Los Angeles Lakers @ San Antonio Spurs", 0));
        gameThreadList.add(makeFakeGameThreadSummary("id4", "Game Thread: Cleveland Cavaliers @ Houston Rockets", 0));
        gameThreadList.add(makeFakeGameThreadSummary("id5", "Game Thread: Bulls @ Warriors", 0));
        gameThreadList.add(makeFakeGameThreadSummary("id6", "Game Thread: Thunder @ Sacramento Kings", 0));
        gameThreadList.add(makeFakeGameThreadSummary("id7", "[POST GAME THREAD] Cleveland Cavaliers @ Houston Rockets", 0));
        gameThreadList.add(makeFakeGameThreadSummary("id8", "[POST-GAME THREAD] Bulls @ Warriors", 0));
        gameThreadList.add(makeFakeGameThreadSummary("id9", "Post-Game Thread: Thunder @ Sacramento Kings", 0));


        String id0 = RedditUtils.findGameThreadId(gameThreadList,
                RedditUtils.POST_GT_TYPE, "ATL", "LAC");
        String id1 = RedditUtils.findGameThreadId(gameThreadList,
                RedditUtils.POST_GT_TYPE, "SAS", "LAL");
        String id2 = RedditUtils.findGameThreadId(gameThreadList,
                RedditUtils.LIVE_GT_TYPE, "ATL", "LAC");
        String id3 = RedditUtils.findGameThreadId(gameThreadList,
                RedditUtils.LIVE_GT_TYPE, "SAS", "LAL");
        String id4 = RedditUtils.findGameThreadId(gameThreadList,
                RedditUtils.LIVE_GT_TYPE, "HOU", "CLE");
        String id5 = RedditUtils.findGameThreadId(gameThreadList,
                RedditUtils.LIVE_GT_TYPE, "GSW", "CHI");
        String id6 = RedditUtils.findGameThreadId(gameThreadList,
                RedditUtils.LIVE_GT_TYPE, "SAC", "OKC");
        String id7 = RedditUtils.findGameThreadId(gameThreadList,
                RedditUtils.POST_GT_TYPE, "HOU", "CLE");
        String id8 = RedditUtils.findGameThreadId(gameThreadList,
                RedditUtils.POST_GT_TYPE, "GSW", "CHI");
        String id9 = RedditUtils.findGameThreadId(gameThreadList,
                RedditUtils.POST_GT_TYPE, "SAC", "OKC");
        String id10 = RedditUtils.findGameThreadId(gameThreadList,
                RedditUtils.POST_GT_TYPE, "DAL", "NYK");

        assertEquals("id0", id0);
        assertEquals("id1", id1);
        assertEquals("id2", id2);
        assertEquals("id3", id3);
        assertEquals("id4", id4);
        assertEquals("id5", id5);
        assertEquals("id6", id6);
        assertEquals("id7", id7);
        assertEquals("id8", id8);
        assertEquals("id9", id9);
        assertEquals("", id10);
    }

    @Test
    public void testTitleContainsTeam_FullName() {
        boolean isInTitle = RedditUtils.titleContainsTeam(
                "Game Thread: Los Angeles Lakers @ San Antonio Spurs", "San Antonio Spurs");
        assertTrue(isInTitle);
    }

    @Test
    public void testTitleContainsTeam_ShortName() {
        boolean isInTitle = RedditUtils.titleContainsTeam(
                "Game Thread: Los Angeles Lakers @ Spurs", "San Antonio Spurs");
        assertTrue(isInTitle);
    }

    @Test
    public void testTitleContainsTeam_IncompleteName() {
        boolean isInTitle = RedditUtils.titleContainsTeam(
                "Game Thread: Angeles Lakers @ San Antonio Spurs", "Los Angeles Lakers");
        assertTrue(isInTitle);
    }

    private GameThreadSummary makeFakeGameThreadSummary(String id, String title, long createdUtc) {
        return new GameThreadSummary(id, title, createdUtc);
    }
}
