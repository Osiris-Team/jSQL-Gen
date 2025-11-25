package com.osiris.jsqlgen.ui.hours;

import com.osiris.jsqlgen.jsqlgen.*;
import com.osiris.osiris_vaadin_utils.ui.layouts.HLayout;
import com.osiris.osiris_vaadin_utils.ui.notifications.Notify;
import com.osiris.osiris_vaadin_utils.ui.popups.Popup;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.*;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vaadin.flow.component.grid.dnd.GridDropMode.ON_TOP;

@Route("hours-organizer")
public class HoursOrganizerView extends VerticalLayout {

    private PasswordField githubTokenField = new PasswordField("GitHub Token (PAT)");
    {
        githubTokenField.setValue(Global.getFirst().githubToken);
        githubTokenField.addValueChangeListener(e -> {
            Global g = Global.getFirst();
            g.githubToken = e.getValue();
            g.update();
        });
    }
    private TextField repoField = new TextField("GitHub Repo (e.g., user/repo)");
    {
        repoField.setValue(Global.getFirst().lastRepo);
        repoField.addValueChangeListener(e -> {
            Global g = Global.getFirst();
            g.lastRepo = e.getValue();
            g.update();
        });
    }
    private Button importButton = new Button("Import Commits");
    private Button createButton = new Button("+ Entry");
    {
        createButton.addClickListener(e -> {
            openPopup(this, FakeFile.create());
        });
    }

    private static void openPopup(HoursOrganizerView view, FakeFile obj) {
        var p = new Popup();
        var c = obj.toComp();
        c.cbParentFakeFile.setItemLabelGenerator(obj2 -> {
            return obj2.name/* This columns table must contain only references too if you want to fetch their minimal string content */;
        });
        ;
        c.cbParentFakeFile.setRenderer(new ComponentRenderer<>(obj2 -> {
            Div div = new Div();
            div.setText(obj2.name/* This columns table must contain only references too if you want to fetch their minimal string content */);
            return div;}));
        c.btnAdd.addClickListener(e_ -> {
            view.refreshTree();
        });
        c.btnSave.addClickListener(e_ -> {
            Notify.info("Refresh the page to view changes.");
            //view.refreshTree();
        });
        c.btnDelete.addClickListener(e_ -> {
            Notify.info("Refresh the page to view changes.");
            //view.refreshTree();
        });
        p.setContent(c);
        p.buildAndOpen();
    }

    private Button createButtonTag = new Button("+ Tag");
    {
        createButtonTag.addClickListener(e -> {
            var p = new Popup();
            var obj = Tag.create("name", generateRandomColor());
            var c = obj.toComp();
            c.btnAdd.addClickListener(e_ -> {
                refreshTree();
            });
            p.setContent(c);
            p.buildAndOpen();
        });
    }

    private TreeGrid<FakeFileNode> treeGrid = new TreeGrid<>();
    {
        treeGrid.setMinHeight("90vh");
    }
    private TreeData<FakeFileNode> treeData = new TreeData<>();
    private TreeDataProvider<FakeFileNode> dataProvider = new TreeDataProvider<>(treeData);
    private FakeFileNode draggedNode = null;

    public HoursOrganizerView() {
        var hl = new HLayout(githubTokenField).addAndExpand2(repoField).add2(importButton).widthFull();
        hl.setAlignItems(Alignment.END);

        var isTotalCountRecursive = Global.getFirst().toComp().bsIsTotalCountRecursive;
        isTotalCountRecursive.addValueChangeListener(e -> {
            var g = Global.getFirst();
            g.isTotalCountRecursive = e.getValue();
            g.update();
        });

        var hl2 = new HLayout(createButton, createButtonTag, isTotalCountRecursive);
        hl2.setAlignItems(Alignment.END);
        add(hl, hl2,
            new com.osiris.osiris_vaadin_utils.ui.texts.Text(
                "Create entries/directories to organize your commits that contain hours like \"(4h) commit-message\", move via drag and drop."),
            treeGrid);

        treeGrid.addComponentHierarchyColumn(FakeFileNode::getDisplayComponent).setHeader("Hours");
        treeGrid.setDataProvider(dataProvider);
        treeGrid.setDropMode(ON_TOP);
        treeGrid.setRowsDraggable(true);

        importButton.addClickListener(e -> {
            String token = githubTokenField.getValue();
            String repo = repoField.getValue();
            if (!token.isEmpty() && !repo.isEmpty()) {
                importCommits(token, repo);
                refreshTree();
            }
        });

        treeGrid.addDragStartListener(event -> {
            List<FakeFileNode> selectedNodes = new ArrayList<>(treeGrid.getSelectedItems());
            if (selectedNodes.isEmpty()) {
                draggedNode = event.getDraggedItems().stream().findFirst().orElse(null);
                selectedNodes.add(draggedNode);
            }
        });

        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        treeGrid.addDropListener(event -> {
            FakeFileNode target = event.getDropTargetItem().orElse(null);
            List<FakeFileNode> nodesToMove = new ArrayList<>(treeGrid.getSelectedItems());
            if(draggedNode != null && !nodesToMove.contains(draggedNode))
                nodesToMove.add(draggedNode);

            if (target == null || nodesToMove.isEmpty()) return;

            for (FakeFileNode node : nodesToMove) {
                if (node.equals(target)) {
                    Notify.error("Cannot move a node into itself.");
                    continue;
                }

                if (node.file != null && target.file != null) {
                    moveFakeFile(node.file, target.file); // Persist
                    treeData.removeItem(node);
                    treeData.addItem(target, node);
                    refreshItemAndAllParents(node); // TODO add caching to avoid duplicate refresh
                }
            }

            refreshItemAndAllParents(target);
            treeGrid.deselectAll();
            draggedNode = null;
            Notify.success("Moved " + nodesToMove.size() + " item(s).");
        });

        UI.getCurrent().getElement().executeJs(
            "if (!document.getElementById('hover-expand-style')) {" +
                "const style = document.createElement('style');" +
                "style.id = 'hover-expand-style';" +
                "style.innerHTML = `" +
                ".hover-expand {" +
                "  transition: min-width 0.3s ease;" +
                "  width: 20px;" +
                "  min-width: 20px;" +
                "  overflow: hidden;" +
                "}" +
                ".hover-expand:hover {" +
                "  min-width: 150px !important;" +
                "}`;" +
                "document.head.appendChild(style);" +
                "}"
        );

        refreshTree();
    }

    private void refreshTree() {
        treeData.clear();
        List<FakeFile> roots = FakeFile.whereParentFakeFileId().is(Database.defaultInMemoryOnlyObjId).get();
        for (FakeFile root : roots) {
            FakeFileNode rootNode = new FakeFileNode(null, this, root);
            treeData.addItem(null, rootNode);
            addChildren(rootNode);
        }

        for (FakeFile fakeFile : FakeFile.whereParentFakeFileId().isNot(Database.defaultInMemoryOnlyObjId).get()) {
            if(FakeFile.whereId().is(fakeFile.parentFakeFileId).getFirstOrNull() != null) continue;
            fakeFile.parentFakeFileId = FakeFile.DELETED_PARENT_ID;
            fakeFile.update();
        }

        dataProvider.refreshAll();
    }

    private void addChildren(FakeFileNode parentNode) {
        var children = FakeFile.whereParentFakeFileId().is(parentNode.file.id).get();
        for (FakeFile child : children) {
            FakeFileNode childNode = new FakeFileNode(parentNode, this, child);
            treeData.addItem(parentNode, childNode);
            addChildren(childNode);
        }
    }

    /**
     * Refreshes data without collapsing or changing the scroll state of the UI.
     */
    private void refreshItemAndAllParents(@NotNull FakeFileNode node) {
        FakeFile rootFile = node.file;
        var parentFiles = new LinkedHashSet<FakeFile>();
        while (true) { // Find root parent in database
            parentFiles.add(rootFile);
            FakeFile parentFile = FakeFile.whereId().is(rootFile.parentFakeFileId).getFirstOrNull();
            if (parentFile == null) break;
            rootFile = parentFile;
        }

        FakeFileNode root = node;
        var parents = new LinkedHashSet<FakeFileNode>();
        while (true) { // Find root parent in current UI
            parents.add(root);
            if (root.parent == null) break;
            root = root.parent;
        }

        if(root.file.id != rootFile.id){ // root parent changed in database and is now invalid in UI
            var newRoot = new FakeFileNode(null, this, rootFile);
            treeData.addItem(null, newRoot);
            treeData.moveAfterSibling(root, newRoot);
            treeData.removeItem(root);
            addChildren(newRoot);
            dataProvider.refreshAll();
        } else{
            for (FakeFileNode parent : parents) {
                // Only refreshes direct children, thus this loop
                dataProvider.refreshItem(parent, true);
            }
        }
    }

    public void importCommits(String token, String repoName) {
        try {
            GitHub github = new GitHubBuilder().withOAuthToken(token).build();
            GHRepository repo = github.getRepository(repoName);
            PagedIterable<GHCommit> commits = repo.listCommits();

            var dir = FakeFile.whereName().is(repo.getFullName()).getFirstOrNull();
            if(dir == null) dir = FakeFile.createAndAdd(-1, 0, 0, repo.getFullName(), "", null);

            // Note: This assumes commits are returned in reverse chronological order (most recent first), which GitHub’s API does.
            // If you want to be more defensive, verify order using getCommitDate().
            for (GHCommit commit : commits) {
                var sha1 = commit.getSHA1();
                if (FakeFile.whereCommitSha().is(sha1).getFirstOrNull() != null) {
                    break; // Already in DB, stop further import
                }
                String message = commit.getCommitShortInfo().getMessage();
                int hours = parseHours(message);
                FakeFile file = FakeFile.createAndAdd(dir.id, hours, 0, message, sha1, new Timestamp(commit.getCommitDate().getTime()));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            add(new Text("Error: " + ex.getMessage()));
        }
    }

    public void moveFakeFile(FakeFile source, FakeFile newParent) {
        source.parentFakeFileId = newParent.id;
        source.update();
    }

    private int parseHours(String message) {
        Pattern pattern = Pattern.compile("\\((\\d+)h\\)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    public static class FakeFileNode {
        public static Tag DELETED_TAG = Tag.create("deleted-tag", "black");

        public @Nullable FakeFileNode parent;
        public @NotNull FakeFile file;
        public @NotNull HoursOrganizerView view;

        public FakeFileNode(@Nullable FakeFileNode parent, @NotNull HoursOrganizerView view, @NotNull FakeFile file) {
            this.parent = parent;
            this.file = file;
            this.view = view;
        }

        public Component getDisplayComponent() {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setSpacing(true);
            layout.setAlignItems(Alignment.BASELINE);
            layout.setWidthFull();

            var msTags = Tag.newTableMultiSelect();
            msTags.setLabel("");
            msTags.setValue(TagEntry.whereFakeFileId().is(file.id).get()
                .stream().map(entry -> Tag.whereId().is(entry.tagId).getOptional().orElse(DELETED_TAG))
                .toList());
            msTags.addSelectionListener(e -> {
                for (Tag tag : e.getRemovedSelection()) {
                    TagEntry.whereFakeFileId().is(file.id).and(
                        TagEntry.whereTagId().is(tag.id)
                    ).remove();
                }
                for (Tag fakeFileTag : e.getAddedSelection()) {
                    TagEntry.createAndAdd(file.id, fakeFileTag.id);
                }
                if(parent == null) view.refreshTree();
                else view.refreshItemAndAllParents(parent);
            });
            msTags.setMinWidth("20px");
            msTags.getStyle()
                .set("font-size", "12px")
                .set("padding", "2px")
                .set("margin", "0")
                .set("height", "28px");
            msTags.addClassName("hover-expand");

            layout.add(msTags);

            boolean isDir = isDirectory();
            int totalHours = isDir ? (Global.getFirst().isTotalCountRecursive ? getSumHoursRecursive(file.id, new HashSet<>()) : getSumHours()) : file.hours;

            String title = (isDir ? "(" + totalHours + (file.maxHours > 0 ? "/"+file.maxHours : "") + "h) " : "") + file.name;
            if (!isDir && file.hours > 0) title += " (" + file.hours + "h)";

            var btn = new Button(VaadinIcon.COG.create());
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            layout.add(btn);
            btn.addClickListener(e -> {
                openPopup(view, file);
            });

            Span titleSpan = new Span(title);
            layout.add(titleSpan);
            titleSpan.setMaxWidth("60vw");

            Map<Integer, Integer> tagHours = new LinkedHashMap<>();
            if (isDir) {
                collectTagHoursRecursive(file.id, tagHours, new LinkedHashSet<>());
            } else {
                for (TagEntry tagE : TagEntry.whereFakeFileId().is(file.id).get()) {
                    var tag = Tag.whereId().is(tagE.tagId).getFirstOrNull();
                    if(tag == null) tag = DELETED_TAG;
                    tagHours.put(tag.id, file.hours);
                }
            }

            for (Map.Entry<Integer, Integer> entry : tagHours.entrySet()) {
                int tagId = entry.getKey();
                int hours = entry.getValue();
                var tag = Tag.whereId().is(tagId).getOptional().orElse(DELETED_TAG);
                String color = tag.cssColor;
                Span tagSpan = new Span(tag.name + " (" + hours + "h)");
                tagSpan.getStyle()
                    .set("background-color", color)
                    .set("color", "white")
                    .set("border-radius", "8px")
                    .set("padding", "2px 6px")
                    .set("font-size", "12px");
                layout.add(tagSpan);
            }

            return layout;
        }

        private boolean isDirectory() {
            int childCount = FakeFile.whereParentFakeFileId().is(file.id).get().size();
            return childCount > 0 || file.parentFakeFileId == Database.defaultInMemoryOnlyObjId;
        }

        private int getSumHours() {
            return FakeFile.whereParentFakeFileId().is(file.id).get().stream().mapToInt(c -> c.hours).sum();
        }

        private int getSumHoursRecursive(int fileId, HashSet<Integer> visitedIds) {
            if (visitedIds.contains(fileId)) return 0;
            visitedIds.add(fileId);

            var children = FakeFile.whereParentFakeFileId().is(fileId).get();
            var total = children.stream().mapToInt(c -> c.hours).sum();
            for (FakeFile child : children) {
                total += getSumHoursRecursive(child.id, visitedIds);
            }
            return total;
        }

        private void collectTagHoursRecursive(int fileId, Map<Integer, Integer> tagHours, Set<Integer> visited) {
            if (visited.contains(fileId)) return;
            visited.add(fileId);
            for (FakeFile child : FakeFile.whereParentFakeFileId().is(fileId).get()) {
                for (TagEntry tagE : TagEntry.whereFakeFileId().is(child.id).get()) {
                    var tag = Tag.whereId().is(tagE.tagId).getFirstOrNull();
                    if(tag == null) tag = DELETED_TAG;
                    tagHours.put(tag.id, tagHours.getOrDefault(tag.id, 0) + child.hours);
                }
                collectTagHoursRecursive(child.id, tagHours, visited);
            }
        }
    }

    private static String generateRandomColor() {
        int r = 50 + new Random().nextInt(180);
        int g = 50 + new Random().nextInt(180);
        int b = 50 + new Random().nextInt(180);
        return String.format("rgb(%d,%d,%d)", r, g, b);
    }
}
