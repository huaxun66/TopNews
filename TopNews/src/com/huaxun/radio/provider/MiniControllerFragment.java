/*
 * FFmpegMediaPlayer: A unified interface for playing audio files and streams.
 *
 * Copyright 2014 William Seemann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaxun.radio.provider;
import com.huaxun.R;
import com.huaxun.radio.provider.MusicUtils.ServiceToken;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MiniControllerFragment extends Fragment implements ServiceConnection {
	
	private View mNowPlayingView;
	//private TextView mTitle;
	//private TextView mArtist;
	private ImageView mHandoverButton;
	private ImageView mPauseButton;
	public MediaPlayerFragment mMediaPlayerFragment;
	
    private IMediaPlaybackService mService = null;
    
    private ServiceToken mToken;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
		mToken = MusicUtils.bindToService(getActivity(), this);
		mMediaPlayerFragment = new MediaPlayerFragment();
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mini_controller, container, false);
		mNowPlayingView = view.findViewById(R.id.bigContainer);
		//mTitle = (TextView) view.findViewById(R.id.titleView);
		//mArtist = (TextView) view.findViewById(R.id.subTitleView);
		mPauseButton = (ImageView) view.findViewById(R.id.playPauseView);
		mHandoverButton = (ImageView) view.findViewById(R.id.player_handover);
		
		return view;
	}
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateNowPlaying();
	}
 
    @Override
	public void onResume() {
		super.onResume();
		
        IntentFilter f = new IntentFilter();
        f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
        f.addAction(MediaPlaybackService.META_CHANGED);
        f.addAction(MediaPlaybackService.QUEUE_CHANGED);
        getActivity().registerReceiver(mTrackListListener, f);
	}

	@Override
	public void onPause() {
		super.onPause();
		
        getActivity().unregisterReceiver(mTrackListListener);
	}
	
	@Override
	public void onDestroy() {
		MusicUtils.unbindFromService(mToken);
		mService = null;
		
		super.onDestroy();
	}

    private BroadcastReceiver mTrackListListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	updateNowPlaying();
        }
    };
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mService = IMediaPlaybackService.Stub.asInterface(service);
		updateNowPlaying();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
//		Animation fade_out = AnimationUtils.loadAnimation(getActivity(), R.anim.player_out);
//		mNowPlayingView.startAnimation(fade_out);
		mNowPlayingView.setVisibility(View.GONE);
	}
	
	private void updateNowPlaying() {
		if (mNowPlayingView == null) {
			return;
		}
		try {
			if (true && mService != null && mService.getAudioId() != -1) {
				//mTitle.setSelected(true);
				//mArtist.setSelected(true);

				CharSequence trackName = mService.getTrackName();
				CharSequence artistName = mService.getArtistName();                

				String UNKNOWN_STRING = "Unknown";
				
				if (trackName == null || trackName.equals(UNKNOWN_STRING)) {
					//mTitle.setText(R.string.widget_one_track_info_unavailable);
					
				} else {
					//mTitle.setText(trackName);
				}

				if (artistName == null || artistName.equals(UNKNOWN_STRING)) {
					artistName = mService.getMediaUri();
				}

				//mArtist.setText(artistName);

				mPauseButton.setVisibility(View.VISIBLE);

				if (mService.isPlaying()) {
					mPauseButton.setImageResource(R.drawable.player_pause);
				} else {
					mPauseButton.setImageResource(R.drawable.player_play);
				}

				mPauseButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							if (mService.isPlaying()) {
								mService.pause();
							} else {
								mService.play();
							}
						} catch (RemoteException e) {
						}
					}
				});
				
				mHandoverButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						//addFragment();
					}
				});

				if (mNowPlayingView.getVisibility() != View.VISIBLE) {
//					Animation fade_in = AnimationUtils.loadAnimation(getActivity(), R.anim.player_in);
//					mNowPlayingView.startAnimation(fade_in);
				}

				mNowPlayingView.setVisibility(View.VISIBLE);
				/*mNowPlayingView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Context c = v.getContext();
						c.startActivity(new Intent(c, MediaPlayerActivity.class));
					}
				});*/

				return;
			}
		} catch (RemoteException ex) {
		}
//		Animation fade_out = AnimationUtils.loadAnimation(getActivity(), R.anim.player_out);
//		mNowPlayingView.startAnimation(fade_out);
		mNowPlayingView.setVisibility(View.GONE);
	}
	
	/*private void addFragment() {		
        FragmentTransaction fragmentTransaction = this.getActivity().getFragmentManager()
                .beginTransaction();

        // ��׼����
        // fragmentTransaction
        // .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // fragmentTransaction
        // .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        // fragmentTransaction
        // .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

        // �Զ��嶯��

        fragmentTransaction.setCustomAnimations(
                R.anim.push_left_in,
                R.anim.push_left_out,
                R.anim.push_left_in,
                R.anim.push_left_out);

        fragmentTransaction.add(R.id.bigContainer, (Fragment)mMediaPlayerFragment);

        // ���뵽BackStack��
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }*/
}
