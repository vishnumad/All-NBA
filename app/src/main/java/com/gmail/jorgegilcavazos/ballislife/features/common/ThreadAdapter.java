package com.gmail.jorgegilcavazos.ballislife.features.common;

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
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubmissionWrapper;
import com.gmail.jorgegilcavazos.ballislife.features.model.ThreadItem;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Adapter used to hold all of the comments from a threadId. It also supports loading a header view
 * as the first element of the recycler view.
 * The (optional) header is loaded based on the hasHeader field of the constructor and when true
 * loads a {@link FullCardViewHolder} with information about the submission.
 *
 * Currently used to display comments only in the
 * {@link com.gmail.jorgegilcavazos.ballislife.features.gamethread.GameThreadFragment} and to
 * display comments AND a the card for a full submission in the
 * {@link com.gmail.jorgegilcavazos.ballislife.features.submission.SubmissionActivity}.
 */
public class ThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_SUBMISSION_HEADER = 0;
    public static final int TYPE_COMMENT = 1;
    public static final int TYPE_LOAD_MORE = 2;

    private RedditAuthentication redditAuthentication;
    private Context context;
    private List<ThreadItem> commentsList;
    private boolean hasHeader;
    private OnCommentClickListener commentClickListener;
    private OnSubmissionClickListener submissionClickListener;
    private SubmissionWrapper submissionWrapper;

    private PublishSubject<Comment> commentSaves = PublishSubject.create();
    private PublishSubject<Comment> commentUnsaves = PublishSubject.create();
    private PublishSubject<Comment> upvotes = PublishSubject.create();
    private PublishSubject<Comment> downvotes = PublishSubject.create();
    private PublishSubject<Comment> novotes = PublishSubject.create();
    private PublishSubject<Comment> replies = PublishSubject.create();

    public ThreadAdapter(Context context,
            RedditAuthentication redditAuthentication,
            List<ThreadItem> commentsList,
                         boolean hasHeader) {
        this.context = context;
        this.commentsList = commentsList;
        this.hasHeader = hasHeader;
        this.redditAuthentication = redditAuthentication;
    }

    public void setCommentClickListener(OnCommentClickListener commentClickListener) {
        this.commentClickListener = commentClickListener;
    }

    public void setSubmissionClickListener(OnSubmissionClickListener submissionClickListener) {
        this.submissionClickListener = submissionClickListener;
    }

    public void setSubmissionWrapper(SubmissionWrapper submissionWrapper) {
        this.submissionWrapper = submissionWrapper;
    }

    public void setSubmission(Submission submission) {
        submissionWrapper = new SubmissionWrapper(submission);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view;

        if (viewType == TYPE_SUBMISSION_HEADER) {
            view = inflater.inflate(R.layout.post_layout_card, parent, false);
            return new FullCardViewHolder(view);
        } else if (viewType == TYPE_COMMENT) {
            view = inflater.inflate(R.layout.comment_layout, parent, false);
            return new CommentViewHolder(view);
        } else if (viewType == TYPE_LOAD_MORE) {
            view = inflater.inflate(R.layout.layout_load_more_comments, parent, false);
            return new LoadMoreCommentsHolder(view);
        } else {
            throw new IllegalArgumentException("Invalid view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof FullCardViewHolder) {
            ((FullCardViewHolder) holder).bindData(
                    context, redditAuthentication, submissionWrapper,
                    false,
                    submissionClickListener);
        } else if (holder instanceof CommentViewHolder) {
            final CommentViewHolder commentHolder = (CommentViewHolder) holder;

            final CommentNode commentNode;
            if (hasHeader && submissionWrapper != null) {
                commentNode = commentsList.get(position - 1).getCommentNode();
            } else {
                commentNode = commentsList.get(position).getCommentNode();
            }
            if (commentNode == null) {
                throw new IllegalStateException("CommentNode should not be null");
            }
            commentHolder.bindData(
                    context,
                    commentNode,
                    commentClickListener,
                    redditAuthentication,
                    commentSaves,
                    commentUnsaves,
                    upvotes,
                    downvotes,
                    novotes,
                    replies);
        } else if (holder instanceof LoadMoreCommentsHolder) {
            if (hasHeader) {
                ((LoadMoreCommentsHolder) holder).bindData(commentsList.get(position - 1)
                        .getDepth());
            } else {
                ((LoadMoreCommentsHolder) holder).bindData(commentsList.get(position).getDepth());
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Submission data guaranteed to be not null at this point.
        if (hasHeader) {
            if (position == 0) {
                return TYPE_SUBMISSION_HEADER;
            } else {
                return commentsList.get(position - 1).getType();
            }
        }
        return commentsList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        if (!hasHeader) {
            return null != commentsList ? commentsList.size() : 0;
        } else {
            if (submissionWrapper == null) {
                // Don't show anything until we have a submission to show.
                return 0;
            } else {
                return null != commentsList ? commentsList.size() + 1 : 1;
            }
        }
    }

    public void setData(List<ThreadItem> data) {
        commentsList.clear();
        commentsList.addAll(data);
        notifyDataSetChanged();
    }

    public void addComment(int position, CommentNode comment) {
        if (position == 0) {
            // Coming from a reply to threadId. Show comment in first position.
            commentsList.add(0, new ThreadItem(ThreadAdapter.TYPE_COMMENT, comment, comment
                    .getDepth()));
        } else {
            // Coming from a comment reply, position param is comment adapter position + 1, which
            // means that if there is a header we need to subtract 1 to place comment in desired
            // position.
            if (hasHeader) {
                commentsList.add(position - 1, new ThreadItem(ThreadAdapter.TYPE_COMMENT,
                        comment, comment.getDepth()));
            } else {
                commentsList.add(position, new ThreadItem(ThreadAdapter.TYPE_COMMENT, comment,
                        comment.getDepth()));
            }
        }
        notifyItemInserted(position);
    }

    public Observable<Comment> getCommentSaves() {
        return commentSaves;
    }

    public Observable<Comment> getCommentUnsaves() {
        return commentUnsaves;
    }

    public Observable<Comment> getUpvotes() {
        return upvotes;
    }

    public Observable<Comment> getDownvotes() {
        return downvotes;
    }

    public Observable<Comment> getNovotes() {
        return novotes;
    }

    public Observable<Comment> getReplies() {
        return replies;
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

        public void bindData(
                final Context context,
                final CommentNode commentNode,
                final OnCommentClickListener commentClickListener,
                final RedditAuthentication redditAuthentication,
                PublishSubject<Comment> commentSaves,
                PublishSubject<Comment> commentUnsaves,
                PublishSubject<Comment> upvotes,
                PublishSubject<Comment> downvotes,
                PublishSubject<Comment> novotes,
                PublishSubject<Comment> replies) {
            final Comment comment = commentNode.getComment();
            String author = comment.getAuthor();
            CharSequence body = RedditUtils.bindSnuDown(comment.data("body_html"));
            String timestamp = DateFormatUtil.formatRedditDate(comment.getCreated());
            String score = String.valueOf(comment.getScore());
            String flair = RedditUtils.parseNbaFlair(String.valueOf(comment.getAuthorFlair()));
            String cssClass = RedditUtils
                    .parseCssClassFromFlair(String.valueOf(comment.getAuthorFlair()));
            int flairRes = RedditUtils.getFlairFromCss(cssClass);

            if (commentNode.hasMoreComments()) {
                // This comment has children that are not currently loaded in the tree.
            }

            authorTextView.setText(author);
            bodyTextView.setOnTouchListener((v, event) -> {
                boolean ret = false;
                CharSequence text = ((TextView) v).getText();
                Spannable stext = Spannable.Factory.getInstance().newSpannable(text);
                TextView widget = (TextView) v;
                int action = event.getAction();

                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
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
            commentContentLayout.setOnClickListener(v -> {
                if (rlCommentActions.getVisibility() == View.VISIBLE) {
                    hideActions(context, commentHolder, commentNode);
                } else {
                    showActions(context, commentHolder, commentNode);
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

            btnUpvote.setOnClickListener(v -> {
                if (scoreTextView.getCurrentTextColor() == colorUpvoted) {
                    if (redditAuthentication.isUserLoggedIn()) {
                        scoreTextView.setTextColor(colorNeutral);
                    }
                    if (commentClickListener != null) {
                        commentClickListener.onVoteComment(comment, VoteDirection.NO_VOTE);
                    }
                    novotes.onNext(comment);
                } else {
                    if (redditAuthentication.isUserLoggedIn()) {
                        scoreTextView.setTextColor(colorUpvoted);
                    }
                    if (commentClickListener != null) {
                        commentClickListener.onVoteComment(comment, VoteDirection.UPVOTE);
                    }
                    upvotes.onNext(comment);
                }
                hideActions(context, commentHolder, commentNode);
            });
            btnDownvote.setOnClickListener(v -> {
                if (scoreTextView.getCurrentTextColor() == colorDownvoted) {
                    if (redditAuthentication.isUserLoggedIn()) {
                        scoreTextView.setTextColor(colorNeutral);
                    }
                    if (commentClickListener != null) {
                        commentClickListener.onVoteComment(comment, VoteDirection.NO_VOTE);
                    }
                    novotes.onNext(comment);
                } else {
                    if (redditAuthentication.isUserLoggedIn()) {
                        scoreTextView.setTextColor(colorDownvoted);
                    }
                    if (commentClickListener != null) {
                        commentClickListener.onVoteComment(comment, VoteDirection.DOWNVOTE);
                    }
                    downvotes.onNext(comment);
                }
                hideActions(context, commentHolder, commentNode);
            });
            btnSave.setOnClickListener(v -> {
                if (tvSaved.getVisibility() == View.VISIBLE) {
                    if (commentClickListener != null) {
                        commentClickListener.onUnsaveComment(comment);
                    }
                    commentUnsaves.onNext(comment);
                    tvSaved.setVisibility(View.GONE);
                } else {
                    if (commentClickListener != null) {
                        commentClickListener.onSaveComment(comment);
                    }
                    commentSaves.onNext(comment);
                    tvSaved.setVisibility(View.VISIBLE);
                }
                hideActions(context, commentHolder, commentNode);
            });
            btnReply.setOnClickListener(v -> {
                replies.onNext(comment);
                if (commentClickListener != null) {
                    commentClickListener.onReplyToComment(getAdapterPosition(), comment);
                }
                hideActions(context, commentHolder, commentNode);
            });
        }

        private void hideActions(Context context, CommentViewHolder holder, CommentNode
                commentNode) {
            holder.commentInnerContentLayout.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.white));
            holder.rlCommentActions.setVisibility(View.GONE);
            setBackgroundAndPadding(context, commentNode, holder, false /* dark */);
        }

        private void showActions(Context context, CommentViewHolder holder, CommentNode
                commentNode) {
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

    static class LoadMoreCommentsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.innerLayout) View innerLayout;

        public LoadMoreCommentsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(int depth) {
            setBackgroundAndPadding(depth);
        }

        private void setBackgroundAndPadding(int depth) {
            int padding_in_dp = 5;
            final float scale = itemView.getContext().getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5F);

            // Add color if it is not a top-level comment.
            if (depth > 1) {
                int depthFromZero = depth - 2;
                int res = (depthFromZero) % 5;
                switch (res) {
                    case 0:
                        innerLayout.setBackgroundResource(R.drawable.borderblue);
                        break;
                    case 1:
                        innerLayout.setBackgroundResource(R.drawable.bordergreen);
                        break;
                    case 2:
                        innerLayout.setBackgroundResource(R.drawable.borderbrown);
                        break;
                    case 3:
                        innerLayout.setBackgroundResource(R.drawable.borderorange);
                        break;
                    case 4:
                        innerLayout.setBackgroundResource(R.drawable.borderred);
                }
            } else {
                innerLayout.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R
                        .color.commentBgLight));
            }
            // Add padding depending on level.
            itemView.setPadding(padding_in_px * (depth - 2), 0, 0, 0);
        }
    }
}
