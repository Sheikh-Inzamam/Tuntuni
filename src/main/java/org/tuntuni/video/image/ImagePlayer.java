/*
 * Copyright 2016 Tuntuni.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tuntuni.video.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Sudipto Chandra
 */
public class ImagePlayer extends ImageServer {

    private final ImageView mViewer;
    private BufferedImage mImage;

    public ImagePlayer(ImageView viewer) {
        mViewer = viewer;
        mViewer.setSmooth(true);
    }

    @Override
    public void displayImage(ImageFrame image) {
        if (mViewer != null && image != null) {
            applyImage(image.getBufferedImage());
            final Image fximg = SwingFXUtils.toFXImage(mImage, null);
            Platform.runLater(() -> {
                mViewer.setImage(fximg);
            });
        }
    }

    private void applyImage(BufferedImage image) {
        if (mImage == null
                || mImage.getWidth() != image.getWidth()
                || mImage.getHeight() != image.getHeight()) {
            mImage = image;
            return;
        }
        int p = 2, q = 1, s = p + q;
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                Color color1 = new Color(image.getRGB(x, y));
                Color color2 = new Color(mImage.getRGB(x, y));
                int r = (p * color1.getRed() + q * color2.getRed()) / s;
                int g = (p * color1.getGreen() + q * color2.getGreen()) / s;
                int b = (p * color1.getBlue() + q * color2.getBlue()) / s;
                Color color3 = new Color(r, g, b);
                mImage.setRGB(x, y, color3.getRGB());
            }
        }
    }

}
