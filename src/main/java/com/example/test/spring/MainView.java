package com.example.test.spring;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

@Route("")
@StyleSheet("frontend://styles/styles.css")
@Push
@PWA(name = "Vaadin Chat", shortName = "Chat")
public class MainView extends VerticalLayout {

  private final UnicastProcessor<ChatMessage> publisher;
  private final Flux<ChatMessage> messages;
  private String username;

  public MainView(UnicastProcessor<ChatMessage> publisher, Flux<ChatMessage> messages) {
    this.publisher = publisher;
    this.messages = messages;
    addClassName("main-view");
    setSizeFull();
    setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    H1 header = new H1("Vaadin Chat");
    header.getElement().getThemeList().add("dark");
    add(header);

    askUsername();

  }

  private void askUsername() {
    HorizontalLayout layout = new HorizontalLayout();

    TextField usernameField = new TextField();
    usernameField.setPlaceholder("enter your name..");
    Button startButton = new Button("Start chatting");

    startButton.addClickListener(click -> {
      username = usernameField.getValue();
      remove(layout);
      startChat();
    });

    layout.add(usernameField, startButton);

    add(layout);
  }

  private void startChat() {
    MessageList messageList = new MessageList();

    add(messageList, createInputLayout());

    messages.subscribe(m -> {
      getUI().ifPresent(ui -> ui.access(() -> messageList.add(new Paragraph(m.getFrom() + ": " + m.getMessage()))));
    });

    expand(messageList);
  }

  private Component createInputLayout() {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setWidth("100%");

    TextField messageField = new TextField();
    Button send = new Button("Send");
    send.setThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName());
    send.addClickShortcut(Key.ENTER);

    send.addClickListener(click -> {
      publisher.onNext(new ChatMessage(username, messageField.getValue()));

      messageField.clear();
      messageField.focus();
    });
    messageField.focus();

    layout.add(messageField, send);
    layout.expand(messageField);
    return layout;
  }

}
