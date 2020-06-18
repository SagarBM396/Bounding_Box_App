package com.example.myfirstapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class DragAndDrawFragment extends Fragment implements View.OnClickListener {

    public static DragAndDrawFragment newInstance() {
        return new DragAndDrawFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drag_and_draw, container, false);
        Button undoButton = (Button) v.findViewById(R.id.undo_button);
        Button sendButton = (Button) v.findViewById(R.id.button_send);
        /*try {

            FileInputStream fin = getContext().openFileInput("example");
            int c;
            String temp="";

            while( (c = fin.read()) != -1){
                temp = temp + Character.toString((char)c);
            }
            fin.close();
            Uri uri = Uri.parse(temp);
            BoxDrawingView boxDr =  getActivity().findViewById(R.id.boxView);
            Bitmap bitmap = null;
            try {
                InputStream image_stream;
                image_stream = getContext().getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_image_black_24dp);
                boxDr.bitmap = bitmap;
                BoxDrawingView.backgroundSet = true;
                Toast.makeText(getContext(), "Bitmap created successfully", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }*/


        undoButton.setOnClickListener((View.OnClickListener) this);
        sendButton.setOnClickListener((View.OnClickListener) this);
        return v;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.undo_button:
                ShapeDrawingView boxDr =  getActivity().findViewById(R.id.boxView);
                boxDr.undoAction();
                break;

            case R.id.button_send:
                Toast.makeText(getActivity(),
                        "Send Button Working", Toast.LENGTH_LONG).show();
                break;

            /*case R.id.threeButton:
                // do your code
                break;*/

            default:
                break;
        }
    }
}
