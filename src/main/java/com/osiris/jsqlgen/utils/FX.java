package com.osiris.jsqlgen.utils;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.util.Objects;
import java.util.function.Function;

public class FX {
    public static void sizeFull(Node n) {
        updateSize(n, 100, 100);
    }

    public static void widthFull(Node n) {
        widthPercent(n, 100);
    }

    public static void heightFull(Node n) {
        heightPercent(n, 100);
    }

    public static void updateSize(Node n, int widthPercent, int heightPercent) {
        widthPercent(n, widthPercent);
        heightPercent(n, heightPercent);
    }

    public static void widthPercent(Node n, int widthPercent) {
        Exception ex = new Exception();
        ChangeListener<Number> changeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (n.getParent() == null) {
                    observableValue.removeListener(this);
                    return;
                }
                _widthPercent(n, widthPercent);
            }
        };
        if (n.getParent() == null || n.getScene() == null)
            MyThread.runAsync(() -> {
                try {
                    for (int i = 0; i < 100; i++) {
                        Thread.sleep(100);
                        if (n.getParent() != null && n.getScene() != null) break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(n.getScene() == null){
                    //System.err.println("Scene is null!");
                    //ex.printStackTrace();
                    return;
                }
                _widthPercent(n, widthPercent);
                n.getScene().widthProperty().addListener(changeListener);
            });
        else {
            _widthPercent(n, widthPercent);
            Parent rootParent = n.getParent();
            while(rootParent != null){
                if(rootParent.getParent() == null) break;
                rootParent = rootParent.getParent();
            }
            n.getScene().widthProperty().addListener(changeListener);
        }
    }

    private static Object lock = new Object();

    private static void _widthPercent(Node n, int widthPercent) {
        Objects.requireNonNull(n.getParent());
        Platform.runLater(() -> {
            synchronized (lock){
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
            }
        });
    }

    public static void heightPercent(Node n, int heightPercent) {
        Exception ex = new Exception();
        ChangeListener<Number> changeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (n.getParent() == null) {
                    observableValue.removeListener(this);
                    return;
                }
                _heightPercent(n, heightPercent);
            }
        };
        if (n.getParent() == null || n.getScene() == null)
            MyThread.runAsync(() -> {
                try {
                    for (int i = 0; i < 100; i++) {
                        Thread.sleep(100);
                        if (n.getParent() != null && n.getScene() != null) break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(n.getScene() == null){
                    ex.printStackTrace();
                    return;
                }
                _heightPercent(n, heightPercent);
                n.getScene().heightProperty().addListener(changeListener);
            });
        else {
            _heightPercent(n, heightPercent);
            n.getScene().heightProperty().addListener(changeListener);
        }
    }

    private static void _heightPercent(Node n, int heightPercent) {
        Platform.runLater(() -> {
            synchronized (lock){
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
            }
        });
    }

    public static void widthPercentWindow(Node n, int widthPercent) {
        MyThread.runAsyncSingle(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(100);
                    if (n.getParent() != null) break;
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

    public static void heightPercentWindow(Node n, int heightPercent) {
        MyThread.runAsyncSingle(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(100);
                    if (n.getParent() != null) break;
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

    public static void widthPercentScreen(Node n, int widthPercent) {
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

    public static void heightPercentScreen(Node n, int heightPercent) {
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

    /*
    SCROLL LAYOUTS
     */

    private static ScrollBar getScrollbarComponent(ListView<?> control, Orientation orientation) {
        Node n = control.lookup(".scroll-bar");
        if (n instanceof ScrollBar) {
            final ScrollBar bar = (ScrollBar) n;
            if (bar.getOrientation().equals(orientation)) {
                return bar;
            }
        }

        return null;
    }

    public static <T> ListView<T> smoothScrollingList(){
        ListView<T> listView = new ListView<>();
        smoothHScrollingListView(listView, 0.1);
        return listView;
    }

    public static void smoothScrollingListView(ListView<?> listView, double speed) {
        smoothScrollingListView(listView, speed, Orientation.VERTICAL, bounds -> bounds.getHeight());
    }

    public static void smoothHScrollingListView(ListView<?> listView, double speed) {
        smoothScrollingListView(listView, speed, Orientation.HORIZONTAL, bounds -> bounds.getHeight());
    }

    private  static void smoothScrollingListView(ListView<?> listView, double speed, Orientation orientation, Function<Bounds, Double> sizeFunc) {
        ScrollBar scrollBar = getScrollbarComponent(listView, orientation);
        if (scrollBar == null) {
            return;
        }
        scrollBar.setUnitIncrement(5);
        final double[] frictions = {0.99, 0.1, 0.05, 0.04, 0.03, 0.02, 0.01, 0.04, 0.01, 0.008, 0.008, 0.008, 0.008, 0.0006, 0.0005, 0.00003, 0.00001};
        final double[] pushes = {speed};
        final double[] derivatives = new double[frictions.length];
        final double[] lastVPos = {0};
        Timeline timeline = new Timeline();
        final EventHandler<MouseEvent> dragHandler = event -> timeline.stop();
        final EventHandler<ScrollEvent> scrollHandler = event -> {
            scrollBar.valueProperty().set(lastVPos[0]);
            if (event.getEventType() == ScrollEvent.SCROLL) {
                double direction = event.getDeltaY() > 0 ? -1 : 1;
                for (int i = 0; i < pushes.length; i++) {
                    derivatives[i] += direction * pushes[i];
                }
                if (timeline.getStatus() == Animation.Status.STOPPED) {
                    timeline.play();
                }

            }
            event.consume();
        };
        if (scrollBar.getParent() != null) {
            scrollBar.getParent().addEventHandler(MouseEvent.DRAG_DETECTED, dragHandler);
            scrollBar.getParent().addEventHandler(ScrollEvent.ANY, scrollHandler);
        }
        scrollBar.parentProperty().addListener((o,oldVal, newVal)->{
            if (oldVal != null) {
                oldVal.removeEventHandler(MouseEvent.DRAG_DETECTED, dragHandler);
                oldVal.removeEventHandler(ScrollEvent.ANY, scrollHandler);
            }
            if (newVal != null) {
                newVal.addEventHandler(MouseEvent.DRAG_DETECTED, dragHandler);
                newVal.addEventHandler(ScrollEvent.ANY, scrollHandler);
            }
        });

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(3), (event) -> {
            for (int i = 0; i < derivatives.length; i++) {
                derivatives[i] *= frictions[i];
            }
            for (int i = 1; i < derivatives.length; i++) {
                derivatives[i] += derivatives[i - 1];
            }
            double dy = derivatives[derivatives.length - 1];
            double size = sizeFunc.apply(scrollBar.getLayoutBounds());
            scrollBar.valueProperty().set(Math.min(Math.max(scrollBar.getValue() + dy / size, 0), 1));
            lastVPos[0] = scrollBar.getValue();
            if (Math.abs(dy) < 1) {
                if (Math.abs(dy) < 0.001) {
                    timeline.stop();
                }
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }
}
