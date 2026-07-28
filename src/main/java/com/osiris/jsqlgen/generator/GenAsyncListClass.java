package com.osiris.jsqlgen.generator;

import java.util.LinkedHashSet;

public class GenAsyncListClass {
    public static String s(LinkedHashSet<String> imports) {
        imports.add("import java.util.Collection;");
        imports.add("import java.util.Objects;");
        imports.add("import java.util.concurrent.CopyOnWriteArrayList;");
        imports.add("import java.util.concurrent.ExecutorService;");
        imports.add("import java.util.concurrent.Executors;");
        imports.add("import java.util.function.Consumer;");

        return """
             /**
             * Shared global executor service used by default across all instances.
             * Uses a cached thread pool that creates threads on demand and reuses idle threads.
             */
            public static volatile ExecutorService GLOBAL_EXECUTOR = Executors.newCachedThreadPool();

            /**
             * An extension of {@link CopyOnWriteArrayList} that executes {@link #forEach(Consumer)}
             * actions asynchronously across an {@link ExecutorService}.
             *
             * <p>By default, all instances share a single cached thread pool executor to minimize
             * resource overhead and maximize thread reuse. A custom executor can also be provided
             * per instance if isolation or specific scheduling behavior is required.</p>
             *
             * @param <E> the type of elements held in this collection
             */
            public static class AsyncCopyOnWriteArrayList<E> extends CopyOnWriteArrayList<E> {
                public ExecutorService executor;

                /**
                 * Creates an empty list using the shared {@link #GLOBAL_EXECUTOR}.
                 */
                public AsyncCopyOnWriteArrayList() {
                    this(GLOBAL_EXECUTOR);
                }

                /**
                 * Creates a list containing the elements of the specified collection,
                 * using the shared {@link #GLOBAL_EXECUTOR}.
                 *
                 * @param c the collection of initial elements
                 */
                public AsyncCopyOnWriteArrayList(Collection<? extends E> c) {
                    this(c, GLOBAL_EXECUTOR);
                }

                /**
                 * Creates an empty list using a custom {@link ExecutorService}.
                 *
                 * @param executor the executor to run asynchronous tasks on
                 */
                public AsyncCopyOnWriteArrayList(ExecutorService executor) {
                    super();
                    this.executor = Objects.requireNonNull(executor, "executor must not be null");
                }

                /**
                 * Creates a list containing the elements of the specified collection,
                 * using a custom {@link ExecutorService}.
                 *
                 * @param c the collection of initial elements
                 * @param executor the executor to run asynchronous tasks on
                 */
                public AsyncCopyOnWriteArrayList(Collection<? extends E> c, ExecutorService executor) {
                    super(c);
                    this.executor = Objects.requireNonNull(executor, "executor must not be null");
                }

                /**
                 * Performs the given action for each element of the list asynchronously.
                 * <p>
                 * A snapshot of the list is taken at the moment of invocation to maintain
                 * thread safety without blocking concurrent modifications. Each element's
                 * action is submitted as an independent task to the executor.
                 * </p>
                 *
                 * @param action The action to be performed for each element
                 */
                @Override
                public void forEach(Consumer<? super E> action) {
                    Objects.requireNonNull(action, "action must not be null");

                    // Iterating over 'this' uses COWIterator which references the array directly
                    // WITHOUT allocating/cloning a new array!
                    for (E item : this) {
                        executor.execute(() -> action.accept(item));
                    }
                }
            }
        """;
    }
}
