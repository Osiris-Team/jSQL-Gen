package com.osiris.jsqlgen.utils;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.stage.Screen;

import java.util.Objects;

public class FX {
    public static void sizeFull(Node n){
        updateSize(n, 100, 100);
    }
    public static void widthFull(Node n){
        widthPercent(n, 100);
    }
    public static void heightFull(Node n){
        heightPercent(n, 100);
    }

    public static void updateSize(Node n, int widthPercent, int heightPercent) {
        widthPercent(n, widthPercent);
        heightPercent(n, heightPercent);
    }

    public static void widthPercent(Node n, int widthPercent){
        MyThread.runAsyncSingle(() -> {
            try{
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(100);
                    if(n.getParent() != null) break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {

                Objects.requireNonNull(n.getParent());
                Region parent = (Region) n.getParent();
                double parentWidth, parentHeight;
                parentWidth = parent.getWidth();
                parentHeight = parent.getHeight();

                Region target = (Region) n;
                double width = (parentWidth / 100 * widthPercent)
                        - target.paddingProperty().get().getRight();

                target.setMaxWidth(width);
                //target.setMinWidth(width);
                target.setPrefWidth(width);
                target.resize(width, target.getHeight());
            });
        });
    }

    public static void heightPercent(Node n, int heightPercent){
        MyThread.runAsyncSingle(() -> {
            try{
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(100);
                    if(n.getParent() != null) break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                Objects.requireNonNull(n.getParent());
                Region parent = (Region) n.getParent();
                double parentWidth, parentHeight;
                parentWidth = parent.getWidth();
                parentHeight = parent.getHeight();

                Region target = (Region) n;
                double height = parentHeight / 100 * heightPercent
                        - target.paddingProperty().get().getBottom();

                target.setMaxHeight(height);
                //target.setMinHeight(height);
                target.setPrefHeight(height);
                target.resize(target.getWidth(), height);
            });
        });
    }

    public static void widthPercentWindow(Node n, int widthPercent){
        MyThread.runAsyncSingle(() -> {
            try{
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(100);
                    if(n.getParent() != null) break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                Objects.requireNonNull(n.getParent());
                Region parent = (Region) n.getScene().getRoot();
                double parentWidth, parentHeight;
                parentWidth = parent.getWidth();
                parentHeight = parent.getHeight();

                Region target = (Region) n;
                double width = (parentWidth / 100 * widthPercent)
                        - target.paddingProperty().get().getRight();

                target.setMaxWidth(width);
                //target.setMinWidth(width);
                target.setPrefWidth(width);
                target.resize(width, target.getHeight());
            });
        });
    }

    public static void heightPercentWindow(Node n, int heightPercent){
        MyThread.runAsyncSingle(() -> {
            try{
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(100);
                    if(n.getParent() != null) break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                Objects.requireNonNull(n.getParent());
                Region parent = (Region) n.getScene().getRoot();
                double parentWidth, parentHeight;
                parentWidth = parent.getWidth();
                parentHeight = parent.getHeight();

                Region target = (Region) n;
                double height = parentHeight / 100 * heightPercent
                        - target.paddingProperty().get().getBottom();

                target.setMaxHeight(height);
                //target.setMinHeight(height);
                target.setPrefHeight(height);
                target.resize(target.getWidth(), height);
            });
        });
    }

    public static void widthPercentScreen(Node n, int widthPercent){
        Platform.runLater(() -> {
            Region target = (Region) n;
            double width = (Screen.getPrimary().getBounds().getWidth() / 100 * widthPercent)
                    - target.paddingProperty().get().getRight();

            target.setMaxWidth(width);
            //target.setMinWidth(width);
            target.setPrefWidth(width);
            target.resize(width, target.getHeight());
        });
    }

    public static void heightPercentScreen(Node n, int heightPercent){
        Platform.runLater(() -> {
            Region target = (Region) n;
            double height = Screen.getPrimary().getBounds().getHeight() / 100 * heightPercent
                    - target.paddingProperty().get().getBottom();

            target.setMaxHeight(height);
            //target.setMinHeight(height);
            target.setPrefHeight(height);
            target.resize(target.getWidth(), height);
        });
    }
}
