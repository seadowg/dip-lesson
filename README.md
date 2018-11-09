# Using Dependency Inversion to help you (re)write testable code

## Scenario

Imagine you are working on a GUI application. The code for this application
has existed for a while and the team weren't using strict Test Driven Development.
You've recently started working in the codebase and you'd like to write some tests
that let you feel more confident about making changes.

## The object under test

You want to start making changes to the code for a button in the application. This
particular button is one that changes from whatever color is starts out as to
red whenever it's clicked. As we're in Java and our names are long and explicit it's named
`ChangeToRedWhenClickedButton`.

One complexity is that our application runs in a very strict operating environment
where we need to ask permission to respond to clicks events the first time they
happen (similar to using the camera or network in an Android or iOS app).

Here's the code for `ChangeToRedWhenClickedButton`:

```java
public class ChangeToRedWhenClickedButton extends UIButton {

    @Override
    public void onClick() {
        StrictOS.requestClickPermission(new StrictOS.PermissionCallback() {
            @Override
            public void onGranted() {
                setColor(Color.RED);
            }

            @Override
            public void onDenied() {
                // ignored
            }
        });
    }
}
```

You can see that when we respond to the click we have to ask for permission using the static
`requestClickPermission` method. We pass that method a callback that will respond using either
the `onnGranted` or `onDenied` method if the user grants or denies the permission respectively.

Let's write a test for this class:

```java
public class ChangeToRedWhenClickedButtonTest {

    @Test
    public void clickingOnButton_whenPermissionIsGranted_changesToRed() {
        ChangeToRedWhenClickedButton button = new ChangeToRedWhenClickedButton();
        button.setColor(Color.GREEN);

        button.onClick();
        assertEquals(button.getColor(), Color.RED);
    }
}
```

Now let's run it:

```bash
java.lang.IllegalStateException: UI not initialized!

	at internal.os.StrictOS.requestClickPermission(StrictOS.java:5)
	at initial.ChangeToRedWhenClickedButton.onClick(ChangeToRedWhenClickedButton.java:12)
	at initial.ChangeToRedWhenClickedButtonTest.clickingOnButton_whenPermissionIsGranted_changesToRed(ChangeToRedWhenClickedButtonTest.java:16)
```

Ah. That's not good. It seems there is some complexity to calling the `requestClickPermission` method when
the application is not actually running. It would be great if under test we could control the outcome
of `requestPermissionClicked`. This is awkward though as we'd have to mock a static method. We can of course do
that (using libraries such as PowerMock) but often when something is hard to test it can be a sign to stop
and think about why. Is this code over complex? Are the dependencies too tangled? Is it not modular enough? Let's
explore the idea of refactoring our code so that it might be easier to test.

## Dependency Inversion Principle

DIP is a commonly used practice (often accidentally due to test pushing you towards it) within the TDD world as following
it's advice can be useful for designing testable code. DIP has two main components:

**a)** High-level modules should not depend on low-level modules. Both should depend on abstraction.

**b)** Abstractions should not depend on details. Details should depend on abstractions.

OK so what does this mean? So in our example we can think of our `ChangeToRedWhenClickedButton` as a "high-level module"
and our `StrictOS` as a "low-level module". So **(a)** is stating that our button should not "depend" on our OS utility. What
**(b)** is then suggesting is that our button instead depends on an abstraction. This is all pretty academic right now
so let's try following the advice and create an abstraction for our `StrictOS`:

```java
public interface ClickPermissionRequester {
    void request(StrictOS.PermissionCallback permissionCallback);
}
```

We've used an `interface` so we can use a different implementation in our tests and our real code. To do that of
course we will "inject" our dependency so that `ChangeToRedWhenClickedButton` depends on the interface rather
than a concrete implementation:

```java
public class ChangeToRedWhenClickedButton extends UIButton {

    private final ClickPermissionRequester clickPermissionRequester;

    public ChangeToRedWhenClickedButton(ClickPermissionRequester clickPermissionRequester) {
        this.clickPermissionRequester = clickPermissionRequester;
    }

    @Override
    public void onClick() {
        clickPermissionRequester.request(new StrictOS.PermissionCallback() {

            @Override
            public void onGranted() {
                setColor(Color.RED);
            }

            @Override
            public void onDenied() {
                // ignored
            }
        });
    }
}
```

Now for our application we can simply wrap our `StrictOS` interaction in an implementation
of this interface that could be passed to the button in its constructor:

```java
public class StrictOSClickPermissionRequester implements ClickPermissionRequester {
    @Override
    public void request(StrictOS.PermissionCallback permissionCallback) {
        StrictOS.requestClickPermission(permissionCallback);
    }
}
```

And now we can use a fake implementation on our tests:

```java
public class ChangeToRedWhenClickedButtonTest {

    @Test
    public void clickingOnButton_whenPermissionIsGranted_changesToRed() {
        ChangeToRedWhenClickedButton button = new ChangeToRedWhenClickedButton(new GrantedClickPermissionRequester());
        button.setColor(Color.GREEN);

        button.onClick();
        assertEquals(button.getColor(), Color.RED);
    }

    private class GrantedClickPermissionRequester implements ClickPermissionRequester {

        @Override
        public void request(StrictOS.PermissionCallback permissionCallback) {
            permissionCallback.onGranted();
        }
    }
}
```

And it passes! By extracting an abstraction around requesting click permissions we've made our object
more testable.

Of course this also provides other advantages: one for instance is that if the `StrictOS`
object changes it's API then our `ChangeToRedWhenClickedButton` does not have to be changed (unless the entire nature
of the behaviour changes). We're keeping the parts of our code we control protected from the parts we don't.

## Going further

OK so we've learnt some fancy terminology and we've got a test. Have we really improved the code other than for our
testability? Not a lot.

Let's think a little hard about **(b)**: "Abstractions should not depend on details."
If we look at our `ClickPermissionRequester` it's pretty clear it's an abstraction that does "depend on the details". For instance,
in our `ChangeToRedWhenClickedButton` we don't care about the case where permissions aren't granted. But because of the detail
of the `requestClickPermission` method we've made it care about that case. Let's flip this on it's head and create an
abstraction from the viewpoint of our "high level" module:

```
public interface Clicker {
    void click(ClickCallback clickCallback);

    interface ClickCallback {
        void clicked();
    }
}
```

OK so now we have the word "click" in there a few many times but the interface is inherently simpler. Let's create
an implementation for to wrap our `StrictOS` interaction:

```java
public class StrictOSClicker implements Clicker {
    @Override
    public void click(ClickCallback clickCallback) {
        StrictOS.requestClickPermission(new StrictOS.PermissionCallback() {
            @Override
            public void onGranted() {
                clickCallback.clicked();
            }

            @Override
            public void onDenied() {

            }
        });
    }
}
```

And we can update the test:

```java
public class ChangeToRedWhenClickedButtonTest {

    @Test
    public void clickingOnButton_whenPermissionIsGranted_changesToRed() {
        ChangeToRedWhenClickedButton button = new ChangeToRedWhenClickedButton(new FakeClicker());
        button.setColor(Color.GREEN);

        button.onClick();
        assertEquals(button.getColor(), Color.RED);
    }

    private class FakeClicker implements Clicker {
        @Override
        public void click(ClickCallback clickCallback) {
            clickCallback.clicked();
        }
    }
}
```

Now our button doesn't care about the concept that the click can fail. Maybe at some point down the line we'll
need to update our `Clicker` abstraction for that but that can be driven out by the UI layer needing to present
an error or something of that nature.

## Reality check

This example is obviously a little contrived. Here's some points to keep in mind
when applying any of this in the real world:

* Often you end up dealing with objects where you don't have access to the constructor (looking at you Android). This
makes "injecting" the abstractions you pull out a lot harder. You can solve this with public fields with default values or
Dependency Injection/Service Lookup frameworks.
* We'd ideally want tests that check the behaviour at an application level before running through a change like
this. With a more complex example it would be easy to miss a detail when creating your abstractions and end up
with changes in behaviours or errors.
* It's far more ideal to apply this kind of philosophy with a test first approach. In that case our abstractions can be
driven straight from the tests. This allows to not get caught up in the details and get straight to our `Clicker` abstraction.
* As with anything in programming this is just "like, your opinion man". If DIP helps you solve a problem then
that's fantastic. If it doesn't, don't sweat it.