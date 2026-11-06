package at.mci.inphase;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import at.mci.inphase.model.TrainingSession;

public class AthleteTodayFragment extends Fragment {

    private TextView tvAthleteGreeting;
    private TextView tvSelectedDateSubtitle;

    // Top Card (Prognose)
    private MaterialCardView cardPrognosis;
    private TextView tvReadinessStatus;
    private TextView tvReadinessSubtext;
    private CircularProgressIndicator progressReadiness;
    private TextView tvScoreValue;

    // Calendar Strip
    private RecyclerView rvCalendarStrip;
    private Calendar selectedDate;

    // Plan Items
    private MaterialCardView cardDailyCheckin;
    private TextView tvDailyCheckinSubtitle;
    private FrameLayout boxCheckinStatus;
    private TextView tvCheckinCheckmark;

    private LinearLayout layoutTrainingsList;
    private LinearLayout layoutRatingsList;
    private MaterialCardView cardEmptyState;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentTeamId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_athlete_today, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        selectedDate = Calendar.getInstance();

        tvAthleteGreeting = view.findViewById(R.id.tvAthleteGreeting);
        tvSelectedDateSubtitle = view.findViewById(R.id.tvSelectedDateSubtitle);

        cardPrognosis = view.findViewById(R.id.cardPrognosis);
        tvReadinessStatus = view.findViewById(R.id.tvReadinessStatus);
        tvReadinessSubtext = view.findViewById(R.id.tvReadinessSubtext);
        progressReadiness = view.findViewById(R.id.progressReadiness);
        tvScoreValue = view.findViewById(R.id.tvScoreValue);

        rvCalendarStrip = view.findViewById(R.id.rvCalendarStrip);

        cardDailyCheckin = view.findViewById(R.id.cardDailyCheckin);
        tvDailyCheckinSubtitle = view.findViewById(R.id.tvDailyCheckinSubtitle);
        boxCheckinStatus = view.findViewById(R.id.boxCheckinStatus);
        tvCheckinCheckmark = view.findViewById(R.id.tvCheckinCheckmark);

        layoutTrainingsList = view.findViewById(R.id.layoutTrainingsList);
        layoutRatingsList = view.findViewById(R.id.layoutRatingsList);
        cardEmptyState = view.findViewById(R.id.cardEmptyState);

        setupCalendarStrip();

        cardDailyCheckin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DailyReadinessActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void setupCalendarStrip() {
        List<Calendar> weekDays = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        // Start from Monday of current week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            Calendar day = (Calendar) cal.clone();
            weekDays.add(day);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        rvCalendarStrip.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        CalendarStripAdapter calendarAdapter = new CalendarStripAdapter(weekDays, selectedDate, clickedDate -> {
            selectedDate = clickedDate;
            updateDateHeader();
            loadDateSpecificData();
        });
        rvCalendarStrip.setAdapter(calendarAdapter);

        updateDateHeader();
    }

    private void updateDateHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d. MMM", Locale.GERMAN);
        String formattedDate = sdf.format(selectedDate.getTime());

        if (isToday(selectedDate)) {
            tvSelectedDateSubtitle.setText(getString(R.string.today_date_format, formattedDate));
        } else {
            tvSelectedDateSubtitle.setText(formattedDate);
        }
    }

    private boolean isToday(Calendar cal) {
        Calendar today = Calendar.getInstance();
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
               cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

    private void loadDashboardData() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;
            String name = doc.getString("name");
            currentTeamId = doc.getString("teamID");

            if (name != null && !name.isEmpty()) {
                tvAthleteGreeting.setText(getString(R.string.greeting_format, name));
            } else {
                tvAthleteGreeting.setText(getString(R.string.greeting_fallback));
            }

            loadDateSpecificData();
        });
    }

    private void loadDateSpecificData() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        checkReadinessForSelectedDate(userId);
        if (currentTeamId != null) {
            loadTrainingsAndRatingsForSelectedDate(currentTeamId, userId);
        }
    }

    private void checkReadinessForSelectedDate(String userId) {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
        String readinessDocId = userId + "_" + dateStr;

        db.collection("readiness").document(readinessDocId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot doc = task.getResult();
                String trafficLight = doc.getString("trafficLight");
                Long score = doc.getLong("readinessScore");

                updatePrognosisTile(trafficLight, score);

                cardDailyCheckin.setClickable(false);
                cardDailyCheckin.setAlpha(0.85f);
                int scoreVal = score != null ? score.intValue() : 0;
                tvDailyCheckinSubtitle.setText(getString(R.string.checkin_done_sub, scoreVal));

                boxCheckinStatus.setBackgroundResource(R.drawable.bg_checkbox_done);
                tvCheckinCheckmark.setVisibility(View.VISIBLE);
            } else {
                updatePrognosisTile(null, null);

                cardDailyCheckin.setAlpha(1.0f);
                cardDailyCheckin.setClickable(true);
                tvDailyCheckinSubtitle.setText(getString(R.string.checkin_pending_sub));

                boxCheckinStatus.setBackgroundResource(R.drawable.bg_checkbox_pending);
                tvCheckinCheckmark.setVisibility(View.GONE);
            }
        });
    }

    private void updatePrognosisTile(String trafficLight, Long score) {
        if (trafficLight == null || score == null) {
            progressReadiness.setProgressCompat(0, true);
            progressReadiness.setIndicatorColor(Color.parseColor("#D8E2DC"));
            tvScoreValue.setText("--");
            tvReadinessStatus.setText(getString(R.string.prognosis_no_checkin));
            tvReadinessSubtext.setText(getString(R.string.prognosis_no_checkin_sub));
            cardPrognosis.setCardBackgroundColor(Color.parseColor("#2D6A4F"));
            return;
        }

        int scoreVal = score.intValue();
        tvScoreValue.setText(String.format(Locale.getDefault(), "%d%%", scoreVal));
        progressReadiness.setProgressCompat(scoreVal, true);

        switch (trafficLight) {
            case "Green":
                progressReadiness.setIndicatorColor(Color.parseColor("#81C784"));
                tvReadinessStatus.setText(getString(R.string.prognosis_green));
                tvReadinessSubtext.setText(getString(R.string.prognosis_green_sub));
                cardPrognosis.setCardBackgroundColor(Color.parseColor("#2D6A4F"));
                break;
            case "Yellow":
                progressReadiness.setIndicatorColor(Color.parseColor("#FFD54F"));
                tvReadinessStatus.setText(getString(R.string.prognosis_yellow));
                tvReadinessSubtext.setText(getString(R.string.prognosis_yellow_sub));
                cardPrognosis.setCardBackgroundColor(Color.parseColor("#386641"));
                break;
            case "Red":
                progressReadiness.setIndicatorColor(Color.parseColor("#E57373"));
                tvReadinessStatus.setText(getString(R.string.prognosis_red));
                tvReadinessSubtext.setText(getString(R.string.prognosis_red_sub));
                cardPrognosis.setCardBackgroundColor(Color.parseColor("#4A2E2B"));
                break;
            default:
                progressReadiness.setIndicatorColor(Color.parseColor("#D8E2DC"));
                tvReadinessStatus.setText("Status unbekannt");
                tvReadinessSubtext.setText("");
                cardPrognosis.setCardBackgroundColor(Color.parseColor("#2D6A4F"));
                break;
        }
    }

    private void loadTrainingsAndRatingsForSelectedDate(String teamId, String userId) {
        layoutTrainingsList.removeAllViews();
        layoutRatingsList.removeAllViews();

        db.collection("trainings")
            .whereEqualTo("teamID", teamId)
            .get()
            .addOnSuccessListener(querySnaps -> {
                List<TrainingSession> matchingSessions = new ArrayList<>();

                for (DocumentSnapshot doc : querySnaps) {
                    TrainingSession session = doc.toObject(TrainingSession.class);
                    if (session != null) {
                        Calendar sessionCal = Calendar.getInstance();
                        sessionCal.setTimeInMillis(session.getDateTime());

                        if (sessionCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                            sessionCal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)) {
                            matchingSessions.add(session);
                        }
                    }
                }

                if (matchingSessions.isEmpty()) {
                    cardEmptyState.setVisibility(View.VISIBLE);
                } else {
                    cardEmptyState.setVisibility(View.GONE);
                    for (TrainingSession session : matchingSessions) {
                        addTrainingCardToPlan(session);
                        addRatingCardToPlan(session, userId);
                    }
                }
            });
    }

    private void addTrainingCardToPlan(TrainingSession session) {
        if (getContext() == null) return;
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_athlete_plan_training, layoutTrainingsList, false);

        ImageView imgIcon = cardView.findViewById(R.id.imgPlanTrainingIcon);
        TextView tvTitle = cardView.findViewById(R.id.tvPlanTrainingTitle);
        TextView tvType = cardView.findViewById(R.id.tvPlanTrainingType);
        TextView tvTime = cardView.findViewById(R.id.tvPlanTrainingTime);
        TextView tvNotes = cardView.findViewById(R.id.tvPlanTrainingNotes);

        tvTitle.setText(session.getTitle());

        String type = session.getSessionType();
        tvType.setText(java.util.Objects.requireNonNullElse(type, ""));

        int iconColor = MonthCalendarAdapter.getTrainingColor(type);
        int iconBgColor = MonthCalendarAdapter.getTrainingBgColor(type);
        int iconRes = getIconForType(type);

        imgIcon.setImageResource(iconRes);
        imgIcon.setImageTintList(ColorStateList.valueOf(iconColor));

        tvType.setBackgroundTintList(ColorStateList.valueOf(iconBgColor));
        tvType.setTextColor(iconColor);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm 'Uhr'", Locale.getDefault());
        tvTime.setText(sdf.format(new Date(session.getDateTime())));

        if (session.getNotes() != null && !session.getNotes().isEmpty()) {
            tvNotes.setVisibility(View.VISIBLE);
            tvNotes.setText(session.getNotes());
        } else {
            tvNotes.setVisibility(View.GONE);
        }

        layoutTrainingsList.addView(cardView);
    }

    private void addRatingCardToPlan(TrainingSession session, String userId) {
        if (getContext() == null) return;

        float density = getResources().getDisplayMetrics().density;

        MaterialCardView ratingCard = new MaterialCardView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, (int) (12 * density));
        ratingCard.setLayoutParams(params);
        ratingCard.setRadius(16 * density);
        ratingCard.setCardElevation(2 * density);
        ratingCard.setStrokeColor(Color.parseColor("#D8E2DC"));
        ratingCard.setStrokeWidth((int) density);
        ratingCard.setCardBackgroundColor(Color.WHITE);

        LinearLayout content = new LinearLayout(getContext());
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setPadding((int) (16 * density), (int) (16 * density), (int) (16 * density), (int) (16 * density));
        content.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(getContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams((int) (28 * density), (int) (28 * density));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(android.R.drawable.ic_menu_recent_history);
        icon.setColorFilter(Color.parseColor("#2D6A4F"));

        LinearLayout textLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        textParams.setMargins((int) (14 * density), 0, 0, 0);
        textLayout.setLayoutParams(textParams);
        textLayout.setOrientation(LinearLayout.VERTICAL);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(getString(R.string.rate_session_title, session.getTitle()));
        tvTitle.setTextSize(16);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#212529"));

        TextView tvSub = new TextView(getContext());
        tvSub.setText(getString(R.string.rate_pending_sub));
        tvSub.setTextSize(13);
        tvSub.setTextColor(Color.parseColor("#6C757D"));

        textLayout.addView(tvTitle);
        textLayout.addView(tvSub);

        // Uniform 26dp Checkbox Status Box
        FrameLayout boxRatingStatus = new FrameLayout(getContext());
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams((int) (26 * density), (int) (26 * density));
        boxRatingStatus.setLayoutParams(boxParams);
        boxRatingStatus.setBackgroundResource(R.drawable.bg_checkbox_pending);

        TextView tvCheckmark = new TextView(getContext());
        FrameLayout.LayoutParams checkParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        checkParams.gravity = Gravity.CENTER;
        tvCheckmark.setLayoutParams(checkParams);
        tvCheckmark.setText(getString(R.string.checkmark_symbol));
        tvCheckmark.setTextColor(Color.WHITE);
        tvCheckmark.setTextSize(14);
        tvCheckmark.setTypeface(null, android.graphics.Typeface.BOLD);
        tvCheckmark.setVisibility(View.GONE);

        boxRatingStatus.addView(tvCheckmark);

        content.addView(icon);
        content.addView(textLayout);
        content.addView(boxRatingStatus);
        ratingCard.addView(content);

        // Check if workload already exists for this training
        String workloadId = session.getSessionID() + "_" + userId;
        db.collection("workloads").document(workloadId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Long rpe = task.getResult().getLong("rpeScore");
                int rpeVal = rpe != null ? rpe.intValue() : 0;

                ratingCard.setClickable(false);
                ratingCard.setAlpha(0.85f);
                tvSub.setText(getString(R.string.rate_done_sub, rpeVal));

                boxRatingStatus.setBackgroundResource(R.drawable.bg_checkbox_done);
                tvCheckmark.setVisibility(View.VISIBLE);
            } else {
                ratingCard.setAlpha(1.0f);
                ratingCard.setClickable(true);
                tvSub.setText(getString(R.string.rate_pending_sub));

                boxRatingStatus.setBackgroundResource(R.drawable.bg_checkbox_pending);
                tvCheckmark.setVisibility(View.GONE);

                ratingCard.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), RateSessionActivity.class);
                    intent.putExtra("sessionId", session.getSessionID());
                    intent.putExtra("trainingTitle", session.getTitle());
                    startActivity(intent);
                });
            }
        });

        layoutRatingsList.addView(ratingCard);
    }

    private int getIconForType(String type) {
        if (type == null) return R.drawable.ic_runner;
        switch (type) {
            case "Match":
                return R.drawable.ic_match;
            case "Gym":
                return R.drawable.ic_gym;
            case "Recovery":
                return R.drawable.ic_heart_pulse;
            case "Run":
            case "Team_Training":
            default:
                return R.drawable.ic_runner;
        }
    }
}
