package graphics.explorer;

import com.google.gson.Gson;
import graphics.editor.IU_EditorController;
import io.socket.emitter.Emitter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import logic.domain.Project;
import logic.domain.User;
import logic.sockets.SocketProject;
import org.json.JSONArray;
import static tequilaide.TequilaIDE.socket;

/**
 * FXML Controller class
 *
 * @author Miguel Alejandro Cámara Árciga
 */
public class IU_FlowPaneMyProjectsController implements Initializable {

  @FXML
  private FlowPane flowPaneMyProjects;

  Pane pane;

  ArrayList<Pane> projectPanes = new ArrayList<>();

  private List<Project> projectList = new ArrayList<>();

  Stage fileExplorerStage;

  private ResourceBundle rb;
  
  User user;

  /**
   * Initializes the controller class.
   */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    this.rb = rb;
	listenServer();
	SocketProject socketProject = new SocketProject();
	socketProject.loadProjects(user.getIdUsuario());

  }

  public void setUser(User user) {
    this.user = user;
  }
  
  public void setProjectList(List<Project> projectList) {
    this.projectList = projectList;
  }

  /**
   * Método que esta a la escucha de los eventos de entrada y salida del mouse sobre los pane
   * creados por cada prooyecto, es aquí donde se agrega el efecto de cambio de color sobre los
   * iconos de los pane.
   */
  public void hoverListeners() {
    for (int i = 0; i < projectPanes.size(); i++) {
      ImageView im1 = (ImageView) projectPanes.get(i).getChildren().get(0);
      projectPanes.get(i).setOnMouseEntered((e -> im1.setImage(new Image("/resources/icons/proyecto_seleccionado.png"))));
      ImageView im2 = (ImageView) projectPanes.get(i).getChildren().get(0);
      projectPanes.get(i).setOnMouseExited((e -> im2.setImage(new Image("resources/icons/proyecto.png"))));
    }
  }

  /**
   * Método que esta a la escucha de eventos sobre los projectPanes.
   */
  public void projectSelectedAction() {
    for (int i = 0; i < projectPanes.size(); i++) {

      ImageView im1 = (ImageView) projectPanes.get(i).getChildren().get(0);
      Label name = (Label) projectPanes.get(i).getChildren().get(1);
      projectPanes.get(i).setOnMouseClicked((new EventHandler<MouseEvent>() {
        ;
        @Override
        public void handle(MouseEvent e) {
          im1.setImage(new Image("/resources/icons/proyecto_clic.png"));
          Project selectedProject = searchProjectByName(name.getText());
          open_EditorWindow(selectedProject);
        }
      }));
    }
  }

  /**
   * Método que crea iconos para cada elementos de la lista recibida como entrada
   *
   * @param listaProyectos
   */
  public void createIcons(List<Project> listaProyectos) {
    flowPaneMyProjects.setHgap(7);
    for (int i = 0; i < listaProyectos.size(); i++) {
      pane = new Pane();
      pane.setPrefHeight(90);
      pane.setPrefWidth(90);

      ImageView imagev = new ImageView();
      pane.getChildren().add(imagev);
      imagev.setImage(new Image("/resources/icons/proyecto.png"));
      imagev.setFitWidth(58);
      imagev.setFitHeight(58);

      Label projectTitle = new Label();
      projectTitle.setText(listaProyectos.get(i).getNombre());
      projectTitle.setTextFill(Paint.valueOf("white"));
      projectTitle.setLayoutY(72);

      pane.getChildren().add(projectTitle);

      projectPanes.add(pane);
    }
    flowPaneMyProjects.getChildren().clear();
    flowPaneMyProjects.getChildren().addAll(projectPanes);

    System.out.println("iconos creados");
  }

  /**
   * Método que regresa el proyecto cuyo nombre coincida con el parametro de entrada
   *
   * @param name
   * @return
   */
  public Project searchProjectByName(String name) {

    Project proyectoAuxiliar;
    Project selectedProject = null;
    for (int i = 0; i < projectList.size(); i++) {
      proyectoAuxiliar = projectList.get(i);
      if (proyectoAuxiliar.getNombre().equals(name)) {
        selectedProject = proyectoAuxiliar;
      }
    }
    return selectedProject;
  }

  /**
   * Método que ventana IU_Editor.fxml
   *
   * @param selectedProject
   */
  public void open_EditorWindow(Project selectedProject) {
    fileExplorerStage = (Stage) flowPaneMyProjects.getScene().getWindow();
    IU_EditorController controllerObject = new IU_EditorController();
    controllerObject.open_Editor(selectedProject, fileExplorerStage, rb, user);
  }

  public void listenServer(){
	socket.on("projectsRecovered", new Emitter.Listener() {
      @Override
      public void call(Object... os) {
        projectRecovered((boolean) os[0], os[1]);
      }
    });
  }
  
  public void projectRecovered(boolean projectRecovered, Object lista) {
	if (projectRecovered) {
	  System.out.println("recuperando archivos");
	  JSONArray receivedList = (JSONArray) lista;
	  String jsonString = receivedList.toString();

	  Gson gson = new Gson();

	  Project[] jsonProjectList = gson.fromJson(jsonString, Project[].class);
	  projectList = Arrays.asList(jsonProjectList);
	  Platform.runLater(new Runnable() {
		@Override
		public void run() {
		  System.out.println("cargando iconos");
		  createIcons(projectList);
		  hoverListeners();
		  projectSelectedAction();
		}

	  });
	} else {
	  System.out.println((String) lista);
	}
  }
}
