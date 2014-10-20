/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.commons.example;

import java.time.LocalDate;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;

import com.opengamma.basics.date.BusinessDayAdjustment;
import com.opengamma.basics.date.BusinessDayConvention;
import com.opengamma.basics.date.BusinessDayConventions;
import com.opengamma.basics.date.HolidayCalendar;
import com.opengamma.basics.date.HolidayCalendars;
import com.opengamma.basics.schedule.Frequency;
import com.opengamma.basics.schedule.PeriodicScheduleDefn;
import com.opengamma.basics.schedule.PeriodicScheduleException;
import com.opengamma.basics.schedule.RollConvention;
import com.opengamma.basics.schedule.RollConventions;
import com.opengamma.basics.schedule.Schedule;
import com.opengamma.basics.schedule.SchedulePeriod;
import com.opengamma.basics.schedule.SchedulePeriodType;
import com.opengamma.basics.schedule.StubConvention;

/**
 * A simple GUI demonstration of schedule generation using {@link PeriodicScheduleDefn}.
 * <p>
 * This GUI exists for demonstration purposes to aid with understanding schedule generation.
 * It is not intended that is used in a production environment.
 */
public class ScheduleGui extends Application {

  // launch GUI, no arguments needed
  public static void main(String[] args) {
    launch(args);
  }

  //-------------------------------------------------------------------------
  @Override
  public void start(Stage primaryStage) {
    // setup GUI elements
    Label startLbl = new Label("Start date:");
    DatePicker startInp = new DatePicker(LocalDate.now());
    startLbl.setLabelFor(startInp);
    startInp.setShowWeekNumbers(false);
    
    Label endLbl = new Label("End date:");
    DatePicker endInp = new DatePicker(LocalDate.now().plusYears(1));
    endLbl.setLabelFor(endInp);
    endInp.setShowWeekNumbers(false);
    
    Label freqLbl = new Label("Frequency:");
    ChoiceBox<Frequency> freqInp = new ChoiceBox<>(
        FXCollections.observableArrayList(
            Frequency.P1M, Frequency.P2M, Frequency.P3M, Frequency.P4M, Frequency.P6M, Frequency.P12M));
    freqLbl.setLabelFor(freqInp);
    freqInp.setValue(Frequency.P3M);
    
    Label stubLbl = new Label("Stub:");
    ObservableList<StubConvention> stubOptions = FXCollections.observableArrayList(StubConvention.values());
    stubOptions.add(0, null);
    ChoiceBox<StubConvention> stubInp = new ChoiceBox<>(stubOptions);
    stubLbl.setLabelFor(stubInp);
    stubInp.setValue(StubConvention.SHORT_INITIAL);
    
    Label rollLbl = new Label("Roll:");
    ChoiceBox<RollConvention> rollInp = new ChoiceBox<>(
        FXCollections.observableArrayList(
            null,
            RollConventions.NONE,
            RollConventions.EOM,
            RollConventions.IMM,
            RollConventions.IMMAUD,
            RollConventions.IMMNZD,
            RollConventions.SFE));
    rollLbl.setLabelFor(rollInp);
    rollInp.setValue(RollConventions.NONE);
    
    Label bdcLbl = new Label("Adjust:");
    ChoiceBox<BusinessDayConvention> bdcInp = new ChoiceBox<>(
        FXCollections.observableArrayList(
            BusinessDayConventions.NO_ADJUST,
            BusinessDayConventions.FOLLOWING,
            BusinessDayConventions.MODIFIED_FOLLOWING,
            BusinessDayConventions.PRECEDING,
            BusinessDayConventions.MODIFIED_PRECEDING,
            BusinessDayConventions.MODIFIED_FOLLOWING_BI_MONTHLY,
            BusinessDayConventions.NEAREST));
    bdcLbl.setLabelFor(bdcInp);
    bdcInp.setValue(BusinessDayConventions.MODIFIED_FOLLOWING);
    
    Label holidayLbl = new Label("Holidays:");
    ChoiceBox<HolidayCalendar> holidayInp = new ChoiceBox<>(
        FXCollections.observableArrayList(
            HolidayCalendars.CHZU,
            HolidayCalendars.GBLO,
            HolidayCalendars.EUTA,
            HolidayCalendars.FRPA,
            HolidayCalendars.USGS));
    holidayLbl.setLabelFor(holidayInp);
    holidayInp.setValue(HolidayCalendars.GBLO);
    
    TableView<SchedulePeriod> resultGrid = new TableView<>();
    TableColumn<SchedulePeriod, SchedulePeriodType> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(new TableCallback<>(SchedulePeriod.meta().type()));
    TableColumn<SchedulePeriod, LocalDate> unadjustedCol = new TableColumn<>("Unadjusted dates");
    TableColumn<SchedulePeriod, LocalDate> adjustedCol = new TableColumn<>("Adjusted dates");
    TableColumn<SchedulePeriod, LocalDate> resultUnadjStartCol = new TableColumn<>("Start");
    resultUnadjStartCol.setCellValueFactory(new TableCallback<>(SchedulePeriod.meta().unadjustedStartDate()));
    TableColumn<SchedulePeriod, LocalDate> resultUnadjEndCol = new TableColumn<>("End");
    resultUnadjEndCol.setCellValueFactory(new TableCallback<>(SchedulePeriod.meta().unadjustedEndDate()));
    TableColumn<SchedulePeriod, LocalDate> resultStartCol = new TableColumn<>("Start");
    resultStartCol.setCellValueFactory(new TableCallback<>(SchedulePeriod.meta().startDate()));
    TableColumn<SchedulePeriod, LocalDate> resultEndCol = new TableColumn<>("End");
    resultEndCol.setCellValueFactory(new TableCallback<>(SchedulePeriod.meta().endDate()));
    unadjustedCol.getColumns().add(resultUnadjStartCol);
    unadjustedCol.getColumns().add(resultUnadjEndCol);
    adjustedCol.getColumns().add(resultStartCol);
    adjustedCol.getColumns().add(resultEndCol);
    resultGrid.getColumns().add(typeCol);
    resultGrid.getColumns().add(unadjustedCol);
    resultGrid.getColumns().add(adjustedCol);
    resultGrid.setPlaceholder(new Label("Schedule not yet generated"));
    
    // setup generation button
    // this uses the GUI thread which is not the best idea
    Button btn = new Button();
    btn.setText("Generate");
    btn.setOnAction(event -> {
      LocalDate start = startInp.getValue();
      LocalDate end = endInp.getValue();
      Frequency freq = freqInp.getValue();
      StubConvention stub = stubInp.getValue();
      RollConvention roll = rollInp.getValue();
      HolidayCalendar holCal = holidayInp.getValue();
      BusinessDayConvention bdc = bdcInp.getValue();
      BusinessDayAdjustment bda = BusinessDayAdjustment.of(bdc, holCal);
      PeriodicScheduleDefn defn = PeriodicScheduleDefn.builder()
          .startDate(start)
          .endDate(end)
          .frequency(freq)
          .businessDayAdjustment(bda)
          .stubConvention(stub)
          .rollConvention(roll)
          .build();
      try {
        Schedule schedule = defn.createSchedule();
        System.out.println(schedule);
        resultGrid.setItems(FXCollections.observableArrayList(schedule.getPeriods()));
      } catch (PeriodicScheduleException ex) {
        resultGrid.setItems(FXCollections.emptyObservableList());
        resultGrid.setPlaceholder(new Label(ex.getMessage()));
        System.out.println(ex.getMessage());
      }
    });
    
    // layout the components
    GridPane gp = new GridPane();
    gp.setHgap(10);
    gp.setVgap(10);
    gp.setPadding(new Insets(0, 10, 0, 10));
    gp.add(startLbl, 1, 1);
    gp.add(startInp, 2, 1);
    gp.add(endLbl, 1, 2);
    gp.add(endInp, 2, 2);
    gp.add(freqLbl, 1, 3);
    gp.add(freqInp, 2, 3);
    gp.add(bdcLbl, 3, 1);
    gp.add(bdcInp, 4, 1);
    gp.add(holidayLbl, 3, 2);
    gp.add(holidayInp, 4, 2);
    gp.add(stubLbl, 3, 3);
    gp.add(stubInp, 4, 3);
    gp.add(rollLbl, 3, 4);
    gp.add(rollInp, 4, 4);
    gp.add(btn, 3, 5, 2, 1);
    gp.add(resultGrid, 1, 7, 4, 1);
    
    BorderPane bp = new BorderPane(gp);
    Scene scene = new Scene(bp, 600, 600);
    
    // launch
    primaryStage.setTitle("Periodic schedule generator");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  //-------------------------------------------------------------------------
  // link Joda-Bean meta property to JavaFX
  static class TableCallback<S extends Bean, T> implements Callback<CellDataFeatures<S,T>, ObservableValue<T>> {
    private final MetaProperty<T> property;

    public TableCallback(MetaProperty<T> property) {
        this.property = property;
    }

    @Override
    public ObservableValue<T> call(CellDataFeatures<S,T> param) {
      return getCellDataReflectively(param.getValue());
    }

    private ObservableValue<T> getCellDataReflectively(S rowData) {
      if (property == null || rowData == null) {
        return null;
      }
      T value = property.get(rowData);
      if (value == null) {
        return null;
      }
      return new ReadOnlyObjectWrapper<T>(value);
    }
  }

}
