package com.osiris.jsqlgen.ui.hours;

import com.osiris.jsqlgen.jsqlgen.Database;
import com.osiris.jsqlgen.jsqlgen.FakeFile;
import com.osiris.jsqlgen.jsqlgen.Global;
import com.osiris.osiris_vaadin_utils.ui.layouts.HLayout;
import com.osiris.osiris_vaadin_utils.ui.notifications.Notify;
import com.osiris.osiris_vaadin_utils.ui.popups.Popup;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.*;

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
    private Button createButton = new Button("+");
    {
        createButton.addClickListener(e -> {
           var p = new Popup();
           var obj = FakeFile.create();
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

        add(hl, new HLayout(createButton, isTotalCountRecursive),
            new com.osiris.osiris_vaadin_utils.ui.texts.Text(
            "Create entries/directories to organize your commits that contain hours like \"(4h) commit-message\", move via drag and drop."),
            treeGrid);


        treeGrid.addHierarchyColumn(FakeFileNode::getDisplayName).setHeader("Hours");
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
            draggedNode = event.getDraggedItems().stream().findFirst().orElse(null);
        });

        treeGrid.addDropListener(event -> {
            FakeFileNode target = event.getDropTargetItem().orElse(null);

            if (draggedNode != null && target != null && draggedNode.file != null && target.file != null) {
                if (draggedNode.file.id == (target.file.id)) {
                    Notify.error("Cannot move into itself.");
                    return;
                }

                moveFakeFile(draggedNode.file, target.file); // Persist change
                treeData.removeItem(draggedNode);
                treeData.addItem(target, draggedNode);
                dataProvider.refreshAll(); // Or use refreshItem(target) and refreshItem(draggedNode) for more fine-grained update, doesnt work...

                Notify.success("Moved successfully");
            }
        });

        // Initial populate
        refreshTree();
    }

    private void refreshTree() {
        treeData.clear();

        List<FakeFile> roots = FakeFile.whereParentFakeFileId().is(Database.defaultInMemoryOnlyObjId).get();

        for (FakeFile root : roots) {
            FakeFileNode rootNode = new FakeFileNode(root);
            treeData.addItem(null, rootNode);
            addChildren(rootNode);
        }

        dataProvider.refreshAll();
    }

    private void addChildren(FakeFileNode parentNode) {
        var children = FakeFile.whereParentFakeFileId().is(parentNode.file.id).get();
        for (FakeFile child : children) {
            FakeFileNode childNode = new FakeFileNode(child);
            treeData.addItem(parentNode, childNode);
            addChildren(childNode);
        }
    }


    public void importCommits(String token, String repoName) {
        try {
            GitHub github = new GitHubBuilder().withOAuthToken(token).build();
            GHRepository repo = github.getRepository(repoName);
            PagedIterable<GHCommit> commits = repo.listCommits();

            var dir = FakeFile.createAndAdd(-1, 0, repo.getFullName(), "");

            for (GHCommit commit : commits) {
                var sha1 = commit.getSHA1();
                if(FakeFile.whereCommitSha().is(sha1).getFirstOrNull() != null) continue;
                String message = commit.getCommitShortInfo().getMessage();
                int hours = parseHours(message);
                FakeFile.createAndAdd(dir.id, hours, message, sha1);
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

    // === Helper UI model ===

    public static class FakeFileNode {
        public @NotNull FakeFile file;

        public FakeFileNode(@NotNull FakeFile file) {
            this.file = file;
        }

        public String getDisplayName() {
            var childCount = FakeFile.whereParentFakeFileId().is(file.id).get().size();
            if (childCount > 0 ||
                file.parentFakeFileId == Database.defaultInMemoryOnlyObjId) { // Is a dir
                int totalHours = 0;
                if(Global.getFirst().isTotalCountRecursive){
                    totalHours = getSumHoursRecursive(file.id, new HashSet<>());
                } else{
                    totalHours = getSumHours();
                }
                return "(" + totalHours + "h) " + file.name + (file.hours > 0 ? " (including self " + file.hours + "h)" : "");
            } else {
                return "• " + file.name + " (" + file.hours + "h)";
            }
        }

        private int getSumHours() {
            return FakeFile.whereParentFakeFileId().is(file.id).get()
                .stream().mapToInt(c -> c.hours).sum();
        }

        private int getSumHoursRecursive(int fileId, HashSet<Integer> visitedIds) {
            if(visitedIds.contains(fileId)) return 0;
            visitedIds.add(fileId);

            var children = FakeFile.whereParentFakeFileId().is(fileId).get();
            var total = children.stream().mapToInt(c -> c.hours).sum();
            for (FakeFile child : children) {
                total += getSumHoursRecursive(child.id, visitedIds);
            }
            return total;
        }
    }

    // === SQL Simulation Summary ===
    /*
        CREATE TABLE directories (
            id BIGINT PRIMARY KEY AUTO_INCREMENT,
            name VARCHAR(255),
            parent_id BIGINT,
            FOREIGN KEY (parent_id) REFERENCES directories(id)
        );

        CREATE TABLE commits (
            id BIGINT PRIMARY KEY AUTO_INCREMENT,
            commit_sha VARCHAR(255),
            message TEXT,
            hours INT,
            FakeFile_id BIGINT,
            FOREIGN KEY (FakeFile_id) REFERENCES directories(id)
        );

        -- On commit import:
        INSERT INTO directories (name, parent_id) VALUES ('repo', NULL);
        INSERT INTO commits (commit_sha, message, hours, FakeFile_id) VALUES (...);

        -- On drag & drop FakeFile move:
        UPDATE directories SET parent_id = new_parent_id WHERE id = moved_id;
    */
}

