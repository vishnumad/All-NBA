package com.gmail.jorgegilcavazos.ballislife.features.reply;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;

import static android.app.Activity.RESULT_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23,
        packageName = "com.gmail.jorgegilcavazos.ballislife")
/** Tests for the {@link ReplyActivity} */
public class ReplyActivityTest {
    private static final String COMMENT_TEXT = "Comment text";
    private static final String POSTED_COMMENT_TEXT = "Posted comment text";

    private Resources resources;
    private ReplyActivity activity;

    @Before
    public void setUp() throws Exception {
        resources = RuntimeEnvironment.application.getResources();

        activity = Robolectric.buildActivity(ReplyActivity.class).create().start().resume().get();
    }

    @Test
    public void testOnCreate_titleSetupCorrectly() {
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
        TextView tvComment = (TextView) activity.findViewById(R.id.text_comment);
        EditText etResponse = (EditText) activity.findViewById(R.id.edit_response);
        View separator = activity.findViewById(R.id.view_separator);

        assertEquals(tvComment.getVisibility(), View.GONE);
        assertEquals(etResponse.getVisibility(), View.VISIBLE);
        assertEquals(separator.getVisibility(), View.GONE);
    }

    @Test
    public void testOnCreateOptionsMenu_hasPostOption() {
        RoboMenu roboMenu = new RoboMenu();

        activity.onCreateOptionsMenu(roboMenu);

        MenuItem menuItem = roboMenu.findItem(R.id.action_post);
        assertNotNull(menuItem);
        assertEquals(menuItem.getTitle(), resources.getString(R.string.post));
    }

    @Test
    public void testOnOptionsItemSelected_homeSelected_shouldFinishActivity() {
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        RoboMenuItem roboMenuItem = new RoboMenuItem();
        roboMenuItem.setItemId(android.R.id.home);

        activity.onOptionsItemSelected(roboMenuItem);

        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void testOnOptionsItemSelected_postSelected_shouldSetResultCommentAndFinish() {
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
        RoboMenuItem roboMenuItem = new RoboMenuItem();
        roboMenuItem.setItemId(R.id.action_post);

        activity.onOptionsItemSelected(roboMenuItem);

        assertEquals(ShadowToast.getTextOfLatestToast(),
                resources.getString(R.string.reply_is_empty));
    }

    @Test
    public void onClickBold_withSelectedBody() {
        final String text = "This is a line of text.";
        EditText editText = (EditText) activity.findViewById(R.id.edit_response);
        ImageButton boldBtn = (ImageButton) activity.findViewById(R.id.button_bold);
        editText.setText(text);
        editText.setSelection(10, 14);

        boldBtn.performClick();

        assertEquals("This is a **line** of text.", editText.getText().toString());
        assertEquals(16, editText.getSelectionStart());
    }

    @Test
    public void onClickBold_withoutSelectedBody() {
        final String text = "This is a line of text.";
        EditText editText = (EditText) activity.findViewById(R.id.edit_response);
        ImageButton boldBtn = (ImageButton) activity.findViewById(R.id.button_bold);
        editText.setText(text);
        editText.setSelection(10);

        boldBtn.performClick();

        assertEquals("This is a ****line of text.", editText.getText().toString());
        assertEquals(12, editText.getSelectionStart());
    }

    @Test
    public void onClickItalics_withSelectedBody() {
        final String text = "This is a line of text.";
        EditText editText = (EditText) activity.findViewById(R.id.edit_response);
        ImageButton italicsBtn = (ImageButton) activity.findViewById(R.id.button_italic);
        editText.setText(text);
        editText.setSelection(10, 14);

        italicsBtn.performClick();

        assertEquals("This is a *line* of text.", editText.getText().toString());
        assertEquals(15, editText.getSelectionStart());
    }

    @Test
    public void onClickItalics_withoutSelectedBody() {
        final String text = "This is a line of text.";
        EditText editText = (EditText) activity.findViewById(R.id.edit_response);
        ImageButton italicsBtn = (ImageButton) activity.findViewById(R.id.button_italic);
        editText.setText(text);
        editText.setSelection(10);

        italicsBtn.performClick();

        assertEquals("This is a **line of text.", editText.getText().toString());
        assertEquals(11, editText.getSelectionStart());
    }

    @Test
    public void onClickStrikethrough_withSelectedBody() {
        final String text = "This is a line of text.";
        EditText editText = (EditText) activity.findViewById(R.id.edit_response);
        ImageButton strikeBtn = (ImageButton) activity.findViewById(R.id.button_strikethrough);
        editText.setText(text);
        editText.setSelection(10, 14);

        strikeBtn.performClick();

        assertEquals("This is a ~~line~~ of text.", editText.getText().toString());
        assertEquals(16, editText.getSelectionStart());
    }

    @Test
    public void onClickStrikethrough_withoutSelectedBody() {
        final String text = "This is a line of text.";
        EditText editText = (EditText) activity.findViewById(R.id.edit_response);
        ImageButton strikeBtn = (ImageButton) activity.findViewById(R.id.button_strikethrough);
        editText.setText(text);
        editText.setSelection(10);

        strikeBtn.performClick();

        assertEquals("This is a ~~~~line of text.", editText.getText().toString());
        assertEquals(12, editText.getSelectionStart());
    }

    @Test
    public void onClickQuote_withSelection() {
        final String text = "This is a line of text.";
        EditText editText = (EditText) activity.findViewById(R.id.edit_response);
        ImageButton quoteBtn = (ImageButton) activity.findViewById(R.id.button_quotes);
        editText.setText(text);
        editText.setSelection(5, 10);

        quoteBtn.performClick();

        assertEquals("This > is a line of text.", editText.getText().toString());
        assertEquals(7, editText.getSelectionStart());
    }

    @Test
    public void onClickQuote_withEmptyBody() {
        final String text = "";
        EditText editText = (EditText) activity.findViewById(R.id.edit_response);
        ImageButton quoteBtn = (ImageButton) activity.findViewById(R.id.button_quotes);
        editText.setText(text);

        quoteBtn.performClick();

        assertEquals("> ", editText.getText().toString());
        assertEquals(2, editText.getSelectionStart());
    }

    @Test
    public void onSuperScript() {
        EditText editText = (EditText) activity.findViewById(R.id.edit_response);
        ImageButton superScriptBtn = (ImageButton) activity.findViewById(R.id.button_superscript);
        editText.setText("This is a line of text.");
        editText.setSelection(10, 12);

        superScriptBtn.performClick();

        assertEquals("This is a ^line of text.", editText.getText().toString());
        assertEquals(11, editText.getSelectionStart());
    }

    @Test
    public void onLinkClick_noSelection_openDialog() {
        ImageButton linkButton = (ImageButton) activity.findViewById(R.id.button_link);

        linkButton.performClick();

        ShadowAlertDialog shadowDialog = (ShadowAlertDialog) Shadows.shadowOf(ShadowAlertDialog
                .getLatestDialog());
        EditText editText = shadowDialog.getView().findViewById(R.id.edit_text);
        EditText editLink = shadowDialog.getView().findViewById(R.id.edit_link);
        assertEquals(resources.getString(R.string.add_link), shadowDialog.getTitle());
        assertNotNull(editText);
        assertNotNull(editLink);
    }

    @Test
    public void onLinkClick_linkAdded() {
        EditText editText = (EditText) activity.findViewById(R.id.edit_response);
        editText.setText("This is a line of text.");
        editText.setSelection(10, 14);

        activity.onLinkAdded("line with link", "website.com");

        assertEquals("This is a [line with link](website.com) of text.", editText.getText()
                .toString());
    }
}