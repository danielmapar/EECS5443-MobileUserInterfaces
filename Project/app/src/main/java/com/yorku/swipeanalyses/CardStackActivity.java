package com.yorku.swipeanalyses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CardStackActivity extends AppCompatActivity {

    private static final String TAG = "CardStackActivity";
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;
    private enum FRUIT_TYPE {
            GOOD,
            BAD
    };

    private Long startOfExperiment = null;
    private Long endOfLastSwipe = null;
    private ArrayList<Long> swipeTimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_stack);

        Intent intent = getIntent();
        String userGroupId = intent.getStringExtra("userGroupId");
        String username = intent.getStringExtra("username");

        View buttonsContainer = this.findViewById(R.id.fab_container);
        assert userGroupId != null;
        if (!userGroupId.equals("1")) buttonsContainer.setVisibility(View.GONE);

        CardStackView cardStackView = findViewById(R.id.card_stack_view);

        manager = new CardStackLayoutManager(this, new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {
                Log.d(TAG, "onCardDragging: d=" + direction.name() + " ratio=" + ratio);
            }

            @Override
            public void onCardSwiped(Direction direction) {
                Log.d(TAG, "onCardSwiped: p=" + manager.getTopPosition() + " d=" + direction);

                Long currentTime = System.currentTimeMillis();

                Long diff = endOfLastSwipe == null ? currentTime - startOfExperiment : currentTime - endOfLastSwipe;

                swipeTimes.add(diff);

                if (manager.getTopPosition() == adapter.getItemCount()) {
                    Intent myIntent = new Intent(CardStackActivity.this, FeedbackActivity.class);
                    myIntent.putExtra("username", username);
                    myIntent.putExtra("userGroupId", userGroupId);
                    myIntent.putExtra("swipeTimes", swipeTimes.toString());
                    myIntent.putExtra("experimentDuration", String.valueOf(currentTime - startOfExperiment));
                    startActivity(myIntent);
                    finish();
                }

                endOfLastSwipe = currentTime;

                /*if (direction == Direction.Right){
                    Toast.makeText(CardStackActivity.this, "Direction Right", Toast.LENGTH_SHORT).show();
                }
                if (direction == Direction.Top){
                    Toast.makeText(CardStackActivity.this, "Direction Top", Toast.LENGTH_SHORT).show();
                }
                if (direction == Direction.Left){
                    Toast.makeText(CardStackActivity.this, "Direction Left", Toast.LENGTH_SHORT).show();
                }
                if (direction == Direction.Bottom){
                    Toast.makeText(CardStackActivity.this, "Direction Bottom", Toast.LENGTH_SHORT).show();
                }*/

            }

            @Override
            public void onCardRewound() {
                Log.d(TAG, "onCardRewound: " + manager.getTopPosition());
            }

            @Override
            public void onCardCanceled() {
                Log.d(TAG, "onCardRewound: " + manager.getTopPosition());
            }

            @Override
            public void onCardAppeared(View view, int position) {
                TextView tv = view.findViewById(R.id.item_name);
                Log.d(TAG, "onCardAppeared: " + position + ", nama: " + tv.getText());
            }

            @Override
            public void onCardDisappeared(View view, int position) {
                TextView tv = view.findViewById(R.id.item_name);
                Log.d(TAG, "onCardAppeared: " + position + ", nama: " + tv.getText());
            }
        });
        // Using buttons
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);

        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.FREEDOM);

        manager.setCanScrollHorizontal(userGroupId.equals("2"));
        manager.setCanScrollVertical(userGroupId.equals("3"));
        manager.setOverlayInterpolator(new LinearInterpolator());

        adapter = new CardStackAdapter(addList());
        cardStackView.setLayoutManager(manager);
        cardStackView.setAdapter(adapter);
        cardStackView.setItemAnimator(new DefaultItemAnimator());


        FloatingActionButton skipButton = findViewById(R.id.fab_skip);
        skipButton.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(new AccelerateInterpolator())
                    .build();
            manager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
        });
        FloatingActionButton likeButton = findViewById(R.id.fab_like);
        likeButton.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Right)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(new AccelerateInterpolator())
                    .build();
            manager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
        });

        startOfExperiment = System.currentTimeMillis();
    }

    private List<ItemModel> addList() {
        List<ItemModel> items = new ArrayList<>();

        List<String> fruits = new ArrayList<String>() {{
            add("apple");
            add("melon");
            add("pear");
            add("peach");
            add("orange");
        }};

        for (String fruit : fruits) {
            for(int i = 1; i <= 3; i++) {
                ItemModel fruitModel = getFruit(fruit, i, FRUIT_TYPE.GOOD);
                items.add(fruitModel);
            }
            for(int i = 1; i <= 3; i++) {
                ItemModel fruitModel = getFruit(fruit, i, FRUIT_TYPE.BAD);
                items.add(fruitModel);
            }
        }
        return items;
    }

    public ItemModel getFruit(String fruitName, int index, FRUIT_TYPE type)  {
        String[] randomCities = {"Toronto", "Waterloo", "Vancouver"};
        int ageAddition = type.equals(FRUIT_TYPE.GOOD) ? 1 : 20;
        try {
            String fruitFileName = fruitName + index;
            if (type.equals(FRUIT_TYPE.BAD)) {
                fruitFileName += "bad";
            }
            Field resourceField = R.drawable.class.getDeclaredField(fruitFileName);
            int resourceId = resourceField.getInt(resourceField);
            return new ItemModel(resourceId, fruitName.substring(0, 1).toUpperCase() + fruitName.substring(1), index+ageAddition, randomCities[index-1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
