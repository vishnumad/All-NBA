package com.gmail.jorgegilcavazos.ballislife.features.reply;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.jorgegilcavazos.ballislife.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReplyActivity extends AppCompatActivity {
    public static final int POST_COMMENT_REPLY_REQUEST = 1;
    public static final int POST_SUBMISSION_REPLY_REQUEST = 2;
    public static final String KEY_COMMENT = "Comment";
    public static final String KEY_POSTED_COMMENT = "PostedComment";

    @BindView(R.id.text_comment) TextView tvComment;
    @BindView(R.id.edit_response) EditText etResponse;
    @BindView(R.id.view_separator) View separatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        ButterKnife.bind(this);

        setTitle(R.string.add_comment);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        CharSequence comment = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            comment = extras.getCharSequence(KEY_COMMENT);
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

    private void postComment() {
        String response = etResponse.getText().toString();
        if (response.isEmpty()) {
            Toast.makeText(this, R.string.reply_is_empty, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent();
            intent.putExtra(KEY_POSTED_COMMENT, etResponse.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
