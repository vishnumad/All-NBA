package com.gmail.jorgegilcavazos.ballislife.features.reply;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.BuildConfig;
import com.gmail.jorgegilcavazos.ballislife.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import static android.app.Activity.RESULT_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = 21,
        packageName = "com.gmail.jorgegilcavazos.ballislife")
public class ReplyActivityTest {
    private static final String COMMENT_TEXT = "Comment text";
    private static final String POSTED_COMMENT_TEXT = "Posted comment text";

    private Resources resources;

    @Before
    public void setUp() throws Exception {
        resources = RuntimeEnvironment.application.getResources();
    }

    @Test
    public void testOnCreate_titleSetupCorrectly() {
        ReplyActivity activity = Robolectric.buildActivity(ReplyActivity.class)
                .create()
                .get();

        assertEquals(activity.getTitle(), resources.getString(R.string.add_comment));
    }

    @Test
    public void testOnCreate_withCommentExtra() {
        Bundle extras = new Bundle();
        extras.putCharSequence(ReplyActivity.KEY_COMMENT, COMMENT_TEXT);
        ReplyActivity activity = Robolectric.buildActivity(ReplyActivity.class)
                .withIntent(new Intent().putExtras(extras))
                .create()
                .get();
        TextView tvComment = (TextView) activity.findViewById(R.id.text_comment);
        EditText etResponse = (EditText) activity.findViewById(R.id.edit_response);
        View separator = activity.findViewById(R.id.view_separator);

        assertEquals(tvComment.getText().toString(), COMMENT_TEXT);
        assertEquals(etResponse.getVisibility(), View.VISIBLE);
        assertEquals(separator.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testOnCreate_withoutCommentExtra() {
        ReplyActivity activity = Robolectric.buildActivity(ReplyActivity.class)
                .create()
                .get();
        TextView tvComment = (TextView) activity.findViewById(R.id.text_comment);
        EditText etResponse = (EditText) activity.findViewById(R.id.edit_response);
        View separator = activity.findViewById(R.id.view_separator);

        assertEquals(tvComment.getVisibility(), View.GONE);
        assertEquals(etResponse.getVisibility(), View.VISIBLE);
        assertEquals(separator.getVisibility(), View.GONE);
    }

    @Test
    public void testOnCreateOptionsMenu_hasPostOption() {
        ReplyActivity activity = Robolectric.buildActivity(ReplyActivity.class)
                .create()
                .get();
        RoboMenu roboMenu = new RoboMenu();

        activity.onCreateOptionsMenu(roboMenu);

        MenuItem menuItem = roboMenu.findItem(R.id.action_post);
        assertNotNull(menuItem);
        assertEquals(menuItem.getTitle(), resources.getString(R.string.post));
    }

    @Test
    public void testOnOptionsItemSelected_homeSelected_shouldFinishActivity() {
        ReplyActivity activity = Robolectric.buildActivity(ReplyActivity.class)
                .create()
                .get();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        RoboMenuItem roboMenuItem = new RoboMenuItem();
        roboMenuItem.setItemId(android.R.id.home);

        activity.onOptionsItemSelected(roboMenuItem);

        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void testOnOptionsItemSelected_postSelected_shouldSetResultCommentAndFinish() {
        ReplyActivity activity = Robolectric.buildActivity(ReplyActivity.class)
                .create()
                .get();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        RoboMenuItem roboMenuItem = new RoboMenuItem();
        roboMenuItem.setItemId(R.id.action_post);
        EditText editText = (EditText) activity.findViewById(R.id.edit_response);

        editText.setText(POSTED_COMMENT_TEXT);
        activity.onOptionsItemSelected(roboMenuItem);

        assertEquals(shadowActivity.getResultIntent()
                .getStringExtra(ReplyActivity.KEY_POSTED_COMMENT), POSTED_COMMENT_TEXT);
        assertEquals(shadowActivity.getResultCode(), RESULT_OK);
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void testOnOptionsItemSelected_postSelectedWithEmptyResponse_shouldShowErrorToast() {
        ReplyActivity activity = Robolectric.buildActivity(ReplyActivity.class)
                .create()
                .get();
        RoboMenuItem roboMenuItem = new RoboMenuItem();
        roboMenuItem.setItemId(R.id.action_post);

        activity.onOptionsItemSelected(roboMenuItem);

        assertEquals(ShadowToast.getTextOfLatestToast(),
                resources.getString(R.string.reply_is_empty));
    }
}