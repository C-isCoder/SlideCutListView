package qiqi.love.you;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SildeCutListView.RemoveListener {
    private SildeCutListView mSilde;
    private ArrayAdapter<String> adapter;
    private List<String> mlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initView() {
        mSilde = (SildeCutListView) findViewById(R.id.sildecut_listView);
        mSilde.setRemoveListener(this);
        adapter = new ArrayAdapter<String>(this, R.layout.item_layout, R.id.item_text, mlist);
        mSilde.setAdapter(adapter);
        mSilde.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, mlist.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        if (mlist == null) {
            mlist = new ArrayList<String>();
            for (int i = 0; i < 20; i++) {
                mlist.add("滑动删除" + i);
            }
        }
    }

    @Override
    public void removeItem(SildeCutListView.RemoveDirection direction, int position) {
        adapter.remove(adapter.getItem(position));
        switch (direction) {
            case RIGHT: {
                Toast.makeText(MainActivity.this, "向右删除  " + position, Toast.LENGTH_SHORT).show();
                break;
            }
            case LEFT: {
                Toast.makeText(MainActivity.this, "向左删除   " + position, Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                break;
        }
    }
}
