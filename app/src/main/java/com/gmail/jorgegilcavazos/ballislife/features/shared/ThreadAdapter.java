package com.gmail.jorgegilcavazos.ballislife.features.shared;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.data.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter used to hold all of the comments from a thread. It also supports loading a header view
 * as the first element of the recycler view.
 * The (optional) header is leaded based on the hasHeader field of the constructor and when true
 * loads a {@link FullCardViewHolder} with information about the submission.
 *
 * Currently used to display comments only in the
 * {@link com.gmail.jorgegilcavazos.ballislife.features.gamethread.GameThreadFragment} and to
 * display comments AND a the card for a full submission in the
 * {@link com.gmail.jorgegilcavazos.ballislife.features.submission.SubmissionActivity}.
 */
public class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SUBMISSION_HEADER = 0;
    private static final int TYPE_COMMENT = 1;

    private Context context;
    private List<CommentNode> commentsList;
    private boolean hasHeader;
    private OnCommentClickListener commentClickListener;
    private OnSubmissionClickListener submissionClickListener;
    private CustomSubmission customSubmission;

    public ThreadAdapter(Context context, List<CommentNode> commentsList, boolean hasHeader) {
        this.context = context;
        this.commentsList = commentsList;
        this.hasHeader = hasHeader;
    }

    public void setCommentClickListener(OnCommentClickListener commentClickListener) {
        this.commentClickListener = commentClickListener;
    }

    public void setSubmissionClickListener(OnSubmissionClickListener submissionClickListener) {
        this.submissionClickListener = submissionClickListener;
    }

    public void setCustomSubmission(CustomSubmission customSubmission) {
        this.customSubmission = customSubmission;
    }

    public void setSubmission(Submission submission) {
        customSubmission.setSubmission(submission);
    }

    @Override
    public int getItemViewType(int position) {
        if (!hasHeader) {
            return TYPE_COMMENT;
        } else {
            if (position == 0) {
                return TYPE_SUBMISSION_HEADER;
            } else {
                return TYPE_COMMENT;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view;

        if (viewType == TYPE_SUBMISSION_HEADER) {
            view = inflater.inflate(R.layout.post_layout_card, parent, false);
            return new FullCardViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.comment_layout, parent, false);
            return new CommentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof FullCardViewHolder) {
            ((FullCardViewHolder) holder).bindData(context, customSubmission, false,
                    submissionClickListener);
        } else {
            final CommentViewHolder commentHolder = (CommentViewHolder) holder;

            final CommentNode commentNode;
            if (hasHeader) {
                commentNode = commentsList.get(position - 1);
            } else {
                commentNode = commentsList.get(position);
            }
            commentHolder.bindData(context, commentNode, commentClickListener);
        }
    }

    @Override
    public int getItemCount() {
        if (hasHeader) {
            return null != commentsList ? commentsList.size() + 1 : 1;
        } else {
            return null != commentsList ? commentsList.size() : 0;
        }
    }

    public void swap(List<CommentNode> data) {
        commentsList.clear();
        commentsList.addAll(data);
        notifyDataSetChanged();
    }

    public void addComment(int position, CommentNode comment) {
        commentsList.add(position, comment);
        notifyDataSetChanged();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layout_comment_content) View commentContentLayout;
        @BindView(R.id.layout_comment_inner_content) View commentInnerContentLayout;
        @BindView(R.id.comment_author) TextView authorTextView;
        @BindView(R.id.comment_score) TextView scoreTextView;
        @BindView(R.id.comment_timestamp) TextView timestampTextView;
        @BindView(R.id.comment_body) TextView bodyTextView;
        @BindView(R.id.comment_flair) TextView flairTextView;
        @BindView(R.id.comment_saved) TextView tvSaved;
        @BindView(R.id.image_flair) ImageView ivFlair;
        @BindView(R.id.layout_comment_actions) LinearLayout rlCommentActions;
        @BindView(R.id.button_comment_upvote) ImageButton btnUpvote;
        @BindView(R.id.button_comment_downvote) ImageButton btnDownvote;
        @BindView(R.id.button_comment_save) ImageButton btnSave;
        @BindView(R.id.button_comment_reply) ImageButton btnReply;

        public CommentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindData(final Context context, final CommentNode commentNode,
                             final OnCommentClickListener commentClickListener) {

            final Comment comment = commentNode.getComment();
            String author = comment.getAuthor();
            CharSequence body = RedditUtils.bindSnuDown(comment.data("body_html"));
            String timestamp = DateFormatUtil.formatRedditDate(comment.getCreated());
            String score = String.valueOf(comment.getScore());
            String flair = RedditUtils.parseNbaFlair(String.valueOf(comment.getAuthorFlair()));
            String cssClass = RedditUtils
                    .parseCssClassFromFlair(String.valueOf(comment.getAuthorFlair()));
            int flairRes = RedditUtils.getFlairFromCss(cssClass);

            authorTextView.setText(author);
            bodyTextView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    boolean ret = false;
                    CharSequence text = ((TextView) v).getText();
                    Spannable stext = Spannable.Factory.getInstance().newSpannable(text);
                    TextView widget = (TextView) v;
                    int action = event.getAction();

                    if (action == MotionEvent.ACTION_UP ||
                            action == MotionEvent.ACTION_DOWN) {
                        int x = (int) event.getX();
                        int y = (int) event.getY();

                        x -= widget.getTotalPaddingLeft();
                        y -= widget.getTotalPaddingTop();

                        x += widget.getScrollX();
                        y += widget.getScrollY();

                        Layout layout = widget.getLayout();
                        int line = layout.getLineForVertical(y);
                        int off = layout.getOffsetForHorizontal(line, x);

                        ClickableSpan[] link = stext.getSpans(off, off, ClickableSpan.class);

                        if (link.length != 0) {
                            if (action == MotionEvent.ACTION_UP) {
                                link[0].onClick(widget);
                            }
                            ret = true;
                        }
                    }
                    return ret;
                }
            });
            bodyTextView.setText(body);
            timestampTextView.setText(timestamp);
            scoreTextView.setText(context.getString(R.string.points, score));
            if (flairRes != -1) {
                ivFlair.setImageResource(flairRes);
                ivFlair.setVisibility(View.VISIBLE);
                flairTextView.setVisibility(View.GONE);
            } else {
                flairTextView.setText(flair);
                ivFlair.setVisibility(View.GONE);
                flairTextView.setVisibility(View.VISIBLE);
            }
            rlCommentActions.setVisibility(View.GONE);

            setBackgroundAndPadding(context, commentNode, this, false /* dark */);

            final CommentViewHolder commentHolder = this;

            // On comment click hide/show actions (upvote, downvote, save, etc...).
            commentContentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (rlCommentActions.getVisibility() == View.VISIBLE) {
                        hideActions(context, commentHolder, commentNode);
                    } else {
                        showActions(context, commentHolder, commentNode);
                    }
                }
            });

            final int colorUpvoted = ContextCompat.getColor(context, R.color.commentUpvoted);
            final int colorDownvoted = ContextCompat.getColor(context, R.color.commentDownvoted);
            final int colorNeutral = ContextCompat.getColor(context, R.color.commentNeutral);

            // Set score color base on vote.
            if (comment.getVote() == VoteDirection.UPVOTE) {
                scoreTextView.setTextColor(colorUpvoted);
            } else if (comment.getVote() == VoteDirection.DOWNVOTE) {
                scoreTextView.setTextColor(colorDownvoted);
            } else {
                scoreTextView.setTextColor(colorNeutral);
            }

            // Show saved text if saved
            if (comment.isSaved()) {
                tvSaved.setVisibility(View.VISIBLE);
            } else {
                tvSaved.setVisibility(View.GONE);
            }

            // Add a "*" to indicate that a comment has been edited.
            if (comment.hasBeenEdited()) {
                timestampTextView.setText(timestamp + "*");
            }

            btnUpvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (scoreTextView.getCurrentTextColor() == colorUpvoted) {
                        if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                            scoreTextView.setTextColor(colorNeutral);
                        }
                        commentClickListener.onVoteComment(comment, VoteDirection.NO_VOTE);
                    } else {
                        if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                            scoreTextView.setTextColor(colorUpvoted);
                        }
                        commentClickListener.onVoteComment(comment, VoteDirection.UPVOTE);
                    }
                    hideActions(context, commentHolder, commentNode);
                }
            });
            btnDownvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (scoreTextView.getCurrentTextColor() == colorDownvoted) {
                        if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                            scoreTextView.setTextColor(colorNeutral);
                        }
                        commentClickListener.onVoteComment(comment, VoteDirection.NO_VOTE);
                    } else {
                        if (RedditAuthentication.getInstance().isUserLoggedIn()) {
                            scoreTextView.setTextColor(colorDownvoted);
                        }
                        commentClickListener.onVoteComment(comment, VoteDirection.DOWNVOTE);
                    }
                    hideActions(context, commentHolder, commentNode);
                }
            });
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tvSaved.getVisibility() == View.VISIBLE) {
                        commentClickListener.onUnsaveComment(comment);
                        tvSaved.setVisibility(View.GONE);
                    } else {
                        commentClickListener.onSaveComment(comment);
                        tvSaved.setVisibility(View.VISIBLE);
                    }
                    hideActions(context, commentHolder, commentNode);
                }
            });
            btnReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentClickListener.onReplyToComment(getAdapterPosition(), comment);
                    hideActions(context, commentHolder, commentNode);
                }
            });
        }

        private void hideActions(Context context, CommentViewHolder holder, CommentNode commentNode) {
            holder.commentInnerContentLayout.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.white));
            holder.rlCommentActions.setVisibility(View.GONE);
            setBackgroundAndPadding(context, commentNode, holder, false /* dark */);
        }

        private void showActions(Context context, CommentViewHolder holder, CommentNode commentNode) {
            holder.rlCommentActions.setVisibility(View.VISIBLE);
            holder.commentInnerContentLayout.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.lightGray));
            setBackgroundAndPadding(context, commentNode, holder, true /* dark */);
        }

        private void setBackgroundAndPadding(Context context, CommentNode commentNode,
                                             CommentViewHolder holder, boolean dark) {
            int padding_in_dp = 5;
            final float scale = context.getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5F);

            int depth = commentNode.getDepth(); // From 1

            // Add color if it is not a top-level comment.
            if (depth > 1) {
                int depthFromZero = depth - 2;
                int res = (depthFromZero) % 5;
                switch (res) {
                    case 0:
                        if (dark) {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable.borderbluedark);
                        } else {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable.borderblue);
                        }
                        break;
                    case 1:
                        if (dark) {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable.bordergreendark);
                        } else {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable.bordergreen);
                        }
                        break;
                    case 2:
                        if (dark) {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable.borderbrowndark);
                        } else {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable.borderbrown);
                        }
                        break;
                    case 3:
                        if (dark) {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable.borderorangedark);
                        } else {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable.borderorange);
                        }
                        break;
                    case 4:
                        if (dark) {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable.borderreddark);
                        } else {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable.borderred);
                        }
                        break;
                }
            } else {
                if (dark) {
                    holder.commentInnerContentLayout.setBackgroundColor(
                            ContextCompat.getColor(context, R.color.commentBgDark));
                } else {
                    holder.commentInnerContentLayout.setBackgroundColor(
                            ContextCompat.getColor(context, R.color.commentBgLight));
                }
            }
            // Add padding depending on level.
            holder.commentContentLayout.setPadding(padding_in_px * (depth - 2), 0, 0, 0);
        }
    }
}
