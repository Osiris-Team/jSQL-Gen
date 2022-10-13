package com.osiris.jsqlgen.utils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
        if(n.getParent()==null)
            MyThread.runAsync(() -> {
                try{
                    for (int i = 0; i < 100; i++) {
                        Thread.sleep(100);
                        if(n.getParent() != null) break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                _widthPercent(n, widthPercent);
                n.getScene().widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                        if(n.getParent() == null) {
                            observableValue.removeListener(this);
                            return;
                        }
                        _widthPercent(n, widthPercent);
                    }
                });
            });
        else{
            _widthPercent(n, widthPercent);
            n.getScene().widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                    if(n.getParent() == null) {
                        observableValue.removeListener(this);
                        return;
                    }
                    _widthPercent(n, widthPercent);
                }
            });
        }
    }

    private static void _widthPercent(Node n, int widthPercent){
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
    }

    public static void heightPercent(Node n, int heightPercent){
        if(n.getParent()==null)
            MyThread.runAsync(() -> {
                try{
                    for (int i = 0; i < 100; i++) {
                        Thread.sleep(100);
                        if(n.getParent() != null) break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                _heightPercent(n, heightPercent);
                n.getScene().heightProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                        if(n.getParent() == null) {
                            observableValue.removeListener(this);
                            return;
                        }
                        _heightPercent(n, heightPercent);
                    }
                });
            });
        else{
            _heightPercent(n, heightPercent);
            n.getScene().heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                    if(n.getParent() == null) {
                        observableValue.removeListener(this);
                        return;
                    }
                    _heightPercent(n, heightPercent);
                }
            });
        }
    }

    private static void _heightPercent(Node n, int heightPercent){
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
