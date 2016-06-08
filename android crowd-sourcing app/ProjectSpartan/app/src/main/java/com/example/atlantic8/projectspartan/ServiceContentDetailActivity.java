package com.example.atlantic8.projectspartan;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ServiceContentDetailActivity extends AppCompatActivity {
    TextView textViewStartTime, textViewEndTime, textViewTag, textViewCreateTime;
    EditText editTextContent;
    Button btnOK;
    int start_time,end_time;
    String tag, content, create_time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_content_detail);
        textViewStartTime = (TextView)findViewById(R.id.submit_start_time_detail);
        textViewEndTime = (TextView)findViewById(R.id.submit_end_time_detail);
        textViewTag = (TextView)findViewById(R.id.submit_tag_text_detail);
        textViewCreateTime = (TextView)findViewById(R.id.submit_create_time_detail);
        editTextContent = (EditText)findViewById(R.id.tag_content_detail);
        btnOK = (Button)findViewById(R.id.button_submit_detail);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServiceContentDetailActivity.this, SubmitQueryActivity.class);
                intent.putExtra("start_time", start_time);
                intent.putExtra("end_time", end_time);
                intent.putExtra("tag",tag);
                startActivity(intent);
            }
        });
        Intent it = this.getIntent();
        start_time = it.getIntExtra("start_time",-1);
        end_time = it.getIntExtra("end_time", -1);
        tag = it.getStringExtra("tag");
        content = it.getStringExtra("content");
        create_time = it.getStringExtra("create_time");

        textViewStartTime.setText(Integer.toString(start_time));
        textViewEndTime.setText(Integer.toString(end_time));
        textViewTag.setText(tag);
        textViewCreateTime.setText(create_time);
        editTextContent.setText(content);
    }
}
