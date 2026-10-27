package com.liskovsoft.smartyoutubetv2.common.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class LinkHelperTest {
    @Test
    public void videoUrlsKeepPlaybackPosition() {
        assertEquals("https://youtu.be/nragduYePsQ?t=193", LinkHelper.convertToFullVideoUrl("nragduYePsQ", 193).toString());
        assertEquals("https://www.youtube.com/embed/nragduYePsQ?start=193", LinkHelper.convertToEmbedVideoUrl("nragduYePsQ", 193).toString());
    }

    @Test
    public void channelAndPlaylistUrls() {
        assertEquals("https://www.youtube.com/channel/UC123", LinkHelper.convertToFullChannelUrl("UC123").toString());
        assertEquals("https://www.youtube.com/playlist?list=PL123", LinkHelper.convertToPlaylistUrl("PL123").toString());
    }

    @Test
    public void qrCodeLinkEmbedsData() {
        assertEquals("https://api.qrserver.com/v1/create-qr-code/?data=abc", LinkHelper.toQrCodeLink("abc"));
    }
}
