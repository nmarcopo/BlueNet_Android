package com.example.marco.bluenet_01;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

import static android.content.Context.BIND_AUTO_CREATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link chatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link chatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class chatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    ChatView chatView;
    private CentralService mCentral;// = new CentralService();

    private OnFragmentInteractionListener mListener;
    TextView headerText;
    String headerTextString;
    BluetoothDevice mDevice;
    private boolean mShouldUnbind;

    private static final UUID MSG_SERVICE_UUID = UUID
            .fromString("00001869-0000-1000-8000-00805f9b34fb");
    private static final UUID MSG_CHAR_UUID = UUID.
            fromString("00002a69-0000-1000-8000-00805f9b34fb");

    public chatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment chatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static chatFragment newInstance(String param1, String param2) {
        chatFragment fragment = new chatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /***Gatt related stuff here**/
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mCentral = ((CentralService.LocalBinder) iBinder).getService();
            if(!mCentral.initialize()){
                Log.e("FATAL_ERR", "unable to initialize BLE");
                getActivity().finish();
            }
            Log.d("chatFrag","service connected ");
            Bundle bundle = getArguments();
            mDevice = bundle.getParcelable("device");
            mCentral.connect(mDevice);
            Log.d("chatFrag",mCentral.getSupportedGattServices().toString());
              //      mCentral.getGattService(TEST_SERVICE_UUID).getCharacteristic(TEST_CHAR_UUID).getStringValue(0));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mCentral = null;
            Log.d("chatFrag","service disconnected ");
        }
    };







    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
//        Bundle bundle = this.getArguments();
//        BluetoothDevice mDevice = bundle.getParcelable("device");
        Intent CentralServiceIntent = new Intent(this.getActivity(), CentralService.class);
        getActivity().bindService(CentralServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mCentral = new CentralService();

        //mCentral.getGattService(mCentral.MSG_SERVICE_UUID);

    }

    @Override
    public void onResume(){
        super.onResume();

        Intent CentralServiceIntent = new Intent(this.getActivity(), CentralService.class);
        getActivity().bindService(CentralServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        mCentral = new CentralService();
    }

    @Override
    public void onPause(){
        super.onPause();
        mCentral = null;
        getActivity().unbindService(mServiceConnection);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mCentral = null;
        getActivity().unbindService(mServiceConnection);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_chat, container, false);
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // NOTE : We are calling the onFragmentInteraction() declared in the MainActivity
        // ie we are sending "Fragment 1" as title parameter when fragment1 is activated
        if (mListener != null) {
            mListener.onFragmentInteraction("Dispatch Chat");
        }

        // Here we will can create click listners etc for all the gui elements on the fragment.
        // For eg: Button btn1= (Button) view.findViewById(R.id.frag1_btn1);
        // btn1.setOnclickListener(...

        headerText = view.findViewById(R.id.chat_header);
        checkBundle();

        // Send message through bluenet and receive on device
        chatView = view.findViewById(R.id.chat_view);
        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener() {
            @Override
            public boolean sendMessage(ChatMessage chatMessage) {
                // TODO: Jian, implement write below
                //blueNetInterface.write(chattingWith, chatMessage.getMessage());
                return true; // returns true when it should put message on screen, false if it shouldn't
            }
        });

        // this is how you add messages to the screen
        chatView.addMessage(new ChatMessage("new received message", System.currentTimeMillis(), ChatMessage.Type.RECEIVED));

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void checkBundle(){
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            headerTextString = "Chatting With: " + bundle.getString("chattingName", null);
            headerText.setTextColor(getResources().getColor(R.color.black));
            headerText.setText(headerTextString);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String title);
    }



}
