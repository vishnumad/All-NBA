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
import com.gmail.jorgegilcavazos.ballislife.features.model.CommentItem;
import com.gmail.jorgegilcavazos.ballislife.features.model.CommentWrapper;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubmissionWrapper;
import com.gmail.jorgegilcavazos.ballislife.features.model.ThreadItem;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;
import com.gmail.jorgegilcavazos.ballislife.util.StringUtils;

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
 * <p>
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
    private SubmissionWrapper submissionWrapper;

    private PublishSubject<CommentWrapper> commentSaves = PublishSubject.create();
    private PublishSubject<CommentWrapper> commentUnsaves = PublishSubject.create();
    private PublishSubject<CommentWrapper> upvotes = PublishSubject.create();
    private PublishSubject<CommentWrapper> downvotes = PublishSubject.create();
    private PublishSubject<CommentWrapper> novotes = PublishSubject.create();
    private PublishSubject<CommentWrapper> replies = PublishSubject.create();
    private PublishSubject<Submission> submissionSaves = PublishSubject.create();
    private PublishSubject<Submission> submissionUnsaves = PublishSubject.create();
    private PublishSubject<Submission> submissionUpvotes = PublishSubject.create();
    private PublishSubject<Submission> submissionDownvotes = PublishSubject.create();
    private PublishSubject<Submission> submissionNovotes = PublishSubject.create();
    private PublishSubject<String> submissionContentClicks = PublishSubject.create();
    private PublishSubject<String> commentCollapses = PublishSubject.create();
    private PublishSubject<String> commentUnCollapses = PublishSubject.create();
    private PublishSubject<CommentItem> loadMoreComments = PublishSubject.create();

    public ThreadAdapter(Context context, RedditAuthentication redditAuthentication,
                         List<ThreadItem> commentsList, boolean hasHeader) {
        this.context = context;
        this.commentsList = commentsList;
        this.hasHeader = hasHeader;
        this.redditAuthentication = redditAuthentication;
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
            ((FullCardViewHolder) holder).bindData(context, redditAuthentication,
                    submissionWrapper, submissionSaves, submissionUnsaves, submissionUpvotes,
                    submissionDownvotes, submissionNovotes, submissionContentClicks);
        } else if (holder instanceof CommentViewHolder) {
            final CommentViewHolder commentHolder = (CommentViewHolder) holder;

            final ThreadItem item;
            if (hasHeader && submissionWrapper != null) {
                item = commentsList.get(position - 1);
            } else {
                item = commentsList.get(position);
            }
            commentHolder.bindData(context, item, redditAuthentication, commentSaves,
                    commentUnsaves, upvotes, downvotes, novotes, replies, commentCollapses,
                    commentUnCollapses);
        } else if (holder instanceof LoadMoreCommentsHolder) {
            if (hasHeader) {
                ((LoadMoreCommentsHolder) holder).bindData(commentsList.get(position - 1),
                        loadMoreComments);
            } else {
                ((LoadMoreCommentsHolder) holder).bindData(commentsList.get(position),
                        loadMoreComments);
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

    public void addCommentItem(CommentItem commentItem, String parentId) {
        for (int i = 0; i < commentsList.size(); i++) {
            ThreadItem item = commentsList.get(i);
            if (item.getType() == TYPE_COMMENT) {
                if (item.getCommentItem() != null && item.getCommentItem().getCommentWrapper()
                        .getId().equals(parentId)) {
                    commentItem.setDepth(item.getCommentItem().getDepth() + 1);
                    commentsList.add(i + 1, new ThreadItem(TYPE_COMMENT, commentItem, commentItem
                            .getDepth(), false, false));
                    int adapterPosInserted;
                    if (hasHeader) {
                        adapterPosInserted = i + 2;
                    } else {
                        adapterPosInserted = i + 1;
                    }
                    notifyItemInserted(adapterPosInserted);
                    break;
                }
            }
        }
    }

    public void addCommentItem(CommentItem commentItem) {
        commentsList.add(0, new ThreadItem(TYPE_COMMENT, commentItem, 0, false, false));
        int adapterPosInserted;
        if (hasHeader) {
            adapterPosInserted = 1;
        } else {
            adapterPosInserted = 0;
        }
        notifyItemInserted(adapterPosInserted);
    }

    public void collapseComments(String commentId) {
        boolean collapse = false;
        int depth = Integer.MAX_VALUE;

        int firstCollapse = -1;
        int lastCollapse = -1;
        for (int i = 0; i < commentsList.size(); i++) {
            ThreadItem item = commentsList.get(i);
            if (item.getType() == TYPE_COMMENT && item.getCommentItem().getCommentWrapper().getId
                    ().equals(commentId)) {
                collapse = true;
                depth = item.getCommentItem().getDepth();
            } else {
                int nextDepth;
                if (item.getType() == TYPE_COMMENT) {
                    nextDepth = item.getCommentItem().getDepth();
                } else {
                    nextDepth = item.getDepth();
                }

                if (collapse) {
                    if (nextDepth > depth) {
                        item.setHidden(true);
                        if (firstCollapse == -1) {
                            firstCollapse = i;
                        }
                    } else {
                        lastCollapse = i;
                        break;
                    }
                }
            }
        }

        if (firstCollapse != -1 && lastCollapse != -1) {
            if (hasHeader) {
                notifyItemRangeChanged(firstCollapse + 1, lastCollapse + 1);
            } else {
                notifyItemRangeChanged(firstCollapse, lastCollapse);
            }
        }
    }

    public void unCollapseComments(String commentId) {
        boolean uncollapse = false;
        int depth = Integer.MAX_VALUE;

        int firstCollapse = -1;
        int lastCollapse = -1;
        for (int i = 0; i < commentsList.size(); i++) {
            ThreadItem item = commentsList.get(i);
            if (item.getType() == TYPE_COMMENT && item.getCommentItem().getCommentWrapper().getId
                    ().equals(commentId)) {
                uncollapse = true;
                depth = item.getCommentItem().getDepth();
            } else {
                int nextDepth;
                if (item.getType() == TYPE_COMMENT) {
                    nextDepth = item.getCommentItem().getDepth();
                } else {
                    nextDepth = item.getDepth();
                }

                if (uncollapse) {
                    if (nextDepth > depth) {
                        item.setHidden(false);
                        if (firstCollapse == -1) {
                            firstCollapse = i;
                        }
                    } else {
                        lastCollapse = i;
                        break;
                    }
                }
            }
        }

        if (firstCollapse != -1 && lastCollapse != -1) {
            if (hasHeader) {
                notifyItemRangeChanged(firstCollapse + 1, lastCollapse + 1);
            } else {
                notifyItemRangeChanged(firstCollapse, lastCollapse);
            }
        }
    }

    public void insertItemsBelowParent(List<ThreadItem> items, CommentNode parent) {
        for (int i = 0; i < commentsList.size(); i++) {
            ThreadItem item = commentsList.get(i);
            if (item.getType() == TYPE_LOAD_MORE && item.getCommentItem().getCommentWrapper()
                    .getId().equals(parent.getComment().getId())) {
                commentsList.remove(i);
                if (hasHeader) {
                    notifyItemRemoved(i + 1);
                } else {
                    notifyItemRemoved(i);
                }
                commentsList.addAll(i, items);
                if (hasHeader) {
                    notifyItemRangeInserted(i + 1, items.size());
                } else {
                    notifyItemRangeInserted(i, items.size());
                }
                break;
            }
        }
    }

    public Observable<CommentWrapper> getCommentSaves() {
        return commentSaves;
    }

    public Observable<CommentWrapper> getCommentUnsaves() {
        return commentUnsaves;
    }

    public Observable<CommentWrapper> getUpvotes() {
        return upvotes;
    }

    public Observable<CommentWrapper> getDownvotes() {
        return downvotes;
    }

    public Observable<CommentWrapper> getNovotes() {
        return novotes;
    }

    public Observable<CommentWrapper> getReplies() {
        return replies;
    }

    public Observable<Submission> getSubmissionSaves() {
        return submissionSaves;
    }

    public Observable<Submission> getSubmissionUnsaves() {
        return submissionUnsaves;
    }

    public Observable<Submission> getSubmissionUpvotes() {
        return submissionUpvotes;
    }

    public Observable<Submission> getSubmissionDownvotes() {
        return submissionDownvotes;
    }

    public Observable<Submission> getSubmissionNovotes() {
        return submissionNovotes;
    }

    public Observable<String> getSubmissionContentClicks() {
        return submissionContentClicks;
    }

    public Observable<String> getCommentCollapses() {
        return commentCollapses;
    }

    public Observable<String> getCommentUnCollapses() {
        return commentUnCollapses;
    }

    public Observable<CommentItem> getLoadMoreComments() {
        return loadMoreComments;
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
        @BindView(R.id.collapsedIndicator) TextView collapseIndicator;

        public CommentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindData(final Context context, final ThreadItem threadItem, final
        RedditAuthentication redditAuthentication, PublishSubject<CommentWrapper> commentSaves,
                             PublishSubject<CommentWrapper> commentUnsaves,
                             PublishSubject<CommentWrapper> upvotes,
                             PublishSubject<CommentWrapper> downvotes,
                             PublishSubject<CommentWrapper> novotes,
                             PublishSubject<CommentWrapper> replies, PublishSubject<String>
                                     commentCollapses, PublishSubject<String> commentUncollapses) {
            if (threadItem.getHidden()) {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                        .MATCH_PARENT, 0));
                return;
            } else {
                itemView.setVisibility(View.VISIBLE);
                itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                        .MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            final CommentItem commentItem = threadItem.getCommentItem();
            final CommentWrapper comment = commentItem.getCommentWrapper();
            int depth = commentItem.getDepth();
            String author = comment.getAuthor();
            CharSequence body;
            if (StringUtils.Companion.isNullOrEmpty(comment.getBodyHtml())) {
                body = comment.getBody();
            } else {
                body = RedditUtils.bindSnuDown(comment.getBodyHtml());
            }
            String timestamp = DateFormatUtil.formatRedditDate(comment.getCreated());
            String score = String.valueOf(comment.getScore());
            String flair = RedditUtils.parseNbaFlair(String.valueOf(comment.getAuthorFlair()));
            String cssClass = RedditUtils.parseCssClassFromFlair(String.valueOf(comment
                    .getAuthorFlair()));
            int flairRes = RedditUtils.getFlairFromCss(cssClass);

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

            setBackgroundAndPadding(context, depth, this, false /* dark */);

            if (commentItem.getChildrenCollapsed()) {
                collapseIndicator.setVisibility(View.VISIBLE);
            } else {
                collapseIndicator.setVisibility(View.GONE);
            }

            final CommentViewHolder commentHolder = this;

            // On comment click hide/show actions (upvote, downvote, save, etc...).
            commentContentLayout.setOnClickListener(v -> {
                if (rlCommentActions.getVisibility() == View.VISIBLE) {
                    hideActions(context, commentHolder, depth);
                } else {
                    showActions(context, commentHolder, depth);
                }
            });
            commentContentLayout.setOnLongClickListener(v -> {
                if (commentItem.getChildrenCollapsed()) {
                    commentUncollapses.onNext(comment.getId());
                    commentItem.setChildrenCollapsed(false);
                    collapseIndicator.setVisibility(View.GONE);
                } else {
                    commentCollapses.onNext(comment.getId());
                    commentItem.setChildrenCollapsed(true);
                    collapseIndicator.setVisibility(View.VISIBLE);
                }
                return true;
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

            tvSaved.setVisibility(View.GONE);

            // Add a "*" to indicate that a comment has been edited.
            if (comment.getEdited()) {
                timestampTextView.setText(timestamp + "*");
            }

            btnUpvote.setOnClickListener(v -> {
                if (scoreTextView.getCurrentTextColor() == colorUpvoted) {
                    if (redditAuthentication.isUserLoggedIn()) {
                        scoreTextView.setTextColor(colorNeutral);
                    }
                    novotes.onNext(comment);
                } else {
                    if (redditAuthentication.isUserLoggedIn()) {
                        scoreTextView.setTextColor(colorUpvoted);
                    }
                    upvotes.onNext(comment);
                }
                hideActions(context, commentHolder, depth);
            });
            btnDownvote.setOnClickListener(v -> {
                if (scoreTextView.getCurrentTextColor() == colorDownvoted) {
                    if (redditAuthentication.isUserLoggedIn()) {
                        scoreTextView.setTextColor(colorNeutral);
                    }
                    novotes.onNext(comment);
                } else {
                    if (redditAuthentication.isUserLoggedIn()) {
                        scoreTextView.setTextColor(colorDownvoted);
                    }
                    downvotes.onNext(comment);
                }
                hideActions(context, commentHolder, depth);
            });
            btnSave.setOnClickListener(v -> {
                if (tvSaved.getVisibility() == View.VISIBLE) {
                    commentUnsaves.onNext(comment);
                } else {
                    commentSaves.onNext(comment);
                }
                hideActions(context, commentHolder, depth);
            });
            btnReply.setOnClickListener(v -> {
                replies.onNext(comment);
                hideActions(context, commentHolder, depth);
            });
        }

        private void hideActions(Context context, CommentViewHolder holder, int depth) {
            holder.commentInnerContentLayout.setBackgroundColor(ContextCompat.getColor(context, R
                    .color.white));
            holder.rlCommentActions.setVisibility(View.GONE);
            setBackgroundAndPadding(context, depth, holder, false /* dark */);
        }

        private void showActions(Context context, CommentViewHolder holder, int depth) {
            holder.rlCommentActions.setVisibility(View.VISIBLE);
            holder.commentInnerContentLayout.setBackgroundColor(ContextCompat.getColor(context, R
                    .color.lightGray));
            setBackgroundAndPadding(context, depth, holder, true /* dark */);
        }

        private void setBackgroundAndPadding(Context context, int depth, CommentViewHolder
                holder, boolean dark) {
            int padding_in_dp = 5;
            final float scale = context.getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5F);

            // Add color if it is not a top-level comment.
            if (depth > 1) {
                int depthFromZero = depth - 2;
                int res = (depthFromZero) % 5;
                switch (res) {
                    case 0:
                        if (dark) {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable
                                    .borderbluedark);
                        } else {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable
                                    .borderblue);
                        }
                        break;
                    case 1:
                        if (dark) {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable
                                    .bordergreendark);
                        } else {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable
                                    .bordergreen);
                        }
                        break;
                    case 2:
                        if (dark) {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable
                                    .borderbrowndark);
                        } else {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable
                                    .borderbrown);
                        }
                        break;
                    case 3:
                        if (dark) {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable
                                    .borderorangedark);
                        } else {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable
                                    .borderorange);
                        }
                        break;
                    case 4:
                        if (dark) {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable
                                    .borderreddark);
                        } else {
                            holder.commentInnerContentLayout.setBackgroundResource(R.drawable
                                    .borderred);
                        }
                        break;
                }
            } else {
                if (dark) {
                    holder.commentInnerContentLayout.setBackgroundColor(ContextCompat.getColor
                            (context, R.color.commentBgDark));
                } else {
                    holder.commentInnerContentLayout.setBackgroundColor(ContextCompat.getColor
                            (context, R.color.commentBgLight));
                }
            }
            // Add padding depending on level.
            holder.commentContentLayout.setPadding(padding_in_px * (depth - 2), 0, 0, 0);
        }
    }

    static class LoadMoreCommentsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.innerLayout) View innerLayout;
        @BindView(R.id.loadMoreText) TextView loadMoreText;

        public LoadMoreCommentsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(ThreadItem threadItem, PublishSubject<CommentItem> loadMoreComments) {
            if (threadItem.getHidden()) {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                        .MATCH_PARENT, 0));
                return;
            } else {
                itemView.setVisibility(View.VISIBLE);
                itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                        .MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            loadMoreText.setOnClickListener(v -> {
                threadItem.setLoading(true);
                setLoadingIndicator(true);
                loadMoreComments.onNext(threadItem.getCommentItem());
            });

            setLoadingIndicator(threadItem.getLoading());
            setBackgroundAndPadding(threadItem.getDepth());
        }

        private void setLoadingIndicator(boolean loading) {
            if (loading) {
                loadMoreText.setText(R.string.load_more_comments_loading);
            } else {
                loadMoreText.setText(R.string.load_more_comments);
            }
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
