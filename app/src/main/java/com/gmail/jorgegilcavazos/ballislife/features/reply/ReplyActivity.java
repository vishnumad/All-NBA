package com.gmail.jorgegilcavazos.ballislife.features.reply;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.jorgegilcavazos.ballislife.R;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/** Activity used for adding a comment with formatting tools */
public class ReplyActivity extends AppCompatActivity implements AddLinkDialogFragment
        .OnFragmentInteractionListener {
    public static final int POST_COMMENT_REPLY_REQUEST = 1;
    public static final int POST_SUBMISSION_REPLY_REQUEST = 2;
    public static final String KEY_COMMENT = "Comment";
    public static final String KEY_POSTED_COMMENT = "PostedComment";
    public static final String KEY_COMMENT_ID = "CommentFullname";
    public static final String KEY_SUBMISSION_ID = "SubmissionId";

    @BindView(R.id.text_comment) TextView tvComment;
    @BindView(R.id.edit_response) EditText etResponse;
    @BindView(R.id.view_separator) View separatorView;

    private String parentId;
    private String submissionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        ButterKnife.bind(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setTitle(R.string.add_comment);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        CharSequence comment = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            comment = extras.getCharSequence(KEY_COMMENT);
            parentId = extras.getString(KEY_COMMENT_ID);
            submissionId = extras.getString(KEY_SUBMISSION_ID);
        }

        if (comment == null) {
            tvComment.setVisibility(View.GONE);
            separatorView.setVisibility(View.GONE);
        } else {
            tvComment.setText(comment);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_reply, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_post:
                postComment();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.button_bold)
    public void onClickBold() {
        int start = etResponse.getSelectionStart();
        int end = etResponse.getSelectionEnd();
        String text = etResponse.getText().toString();
        String prevText = text.substring(0, start);
        String actualText = text.substring(start, end);
        String afterText = text.substring(end);
        etResponse.setText(prevText + "**" + actualText + "**" + afterText);
        // Set cursor before the closing '**'.
        etResponse.setSelection(end + 2);
    }

    @OnClick(R.id.button_italic)
    public void onClickItalics() {
        int start = etResponse.getSelectionStart();
        int end = etResponse.getSelectionEnd();
        String text = etResponse.getText().toString();
        String prevText = text.substring(0, start);
        String actualText = text.substring(start, end);
        String afterText = text.substring(end);
        etResponse.setText(prevText + "*" + actualText + "*" + afterText);
        // Set cursor before the closing '*'.
        etResponse.setSelection(end + 1);
    }

    @OnClick(R.id.button_strikethrough)
    public void onClickStrikethrough() {
        int start = etResponse.getSelectionStart();
        int end = etResponse.getSelectionEnd();
        String text = etResponse.getText().toString();
        String prevText = text.substring(0, start);
        String actualText = text.substring(start, end);
        String afterText = text.substring(end);
        etResponse.setText(prevText + "~~" + actualText + "~~" + afterText);
        // Set cursor before the closing '~'.
        etResponse.setSelection(end + 2);
    }

    @OnClick(R.id.button_quotes)
    public void onClickQuote() {
        int start = etResponse.getSelectionStart();
        String text = etResponse.getText().toString();
        String prevText = text.substring(0, start);
        String afterText = text.substring(start);
        etResponse.setText(prevText + "> " + afterText);
        etResponse.setSelection(prevText.length() + 2);
    }

    @OnClick(R.id.button_superscript)
    public void onSuperScript() {
        int start = etResponse.getSelectionStart();
        String text = etResponse.getText().toString();
        String prevText = text.substring(0, start);
        String afterText = text.substring(start);
        etResponse.setText(prevText + "^" + afterText);
        etResponse.setSelection(prevText.length() + 1);
    }

    @OnClick(R.id.button_link)
    public void onClickLink() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);

        AddLinkDialogFragment dialogFragment = AddLinkDialogFragment.Companion.newInstance("");
        dialogFragment.show(fragmentTransaction, "TAG");
    }

    @Override
    public void onLinkAdded(@NotNull String text, @NotNull String link) {
        int start = etResponse.getSelectionStart();
        int end = etResponse.getSelectionEnd();
        String responseText = etResponse.getText().toString();
        String prevText = responseText.substring(0, start);
        String afterText = responseText.substring(end);
        etResponse.setText(prevText + "[" + text + "](" + link + ")" + afterText);
        etResponse.setSelection(prevText.length() + text.length() + link.length() + 4);
    }

    private void postComment() {
        String response = etResponse.getText().toString();
        if (response.isEmpty()) {
            Toast.makeText(this, R.string.reply_is_empty, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent();
            intent.putExtra(KEY_POSTED_COMMENT, etResponse.getText().toString());
            intent.putExtra(KEY_COMMENT_ID, parentId);
            intent.putExtra(KEY_SUBMISSION_ID, submissionId);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
