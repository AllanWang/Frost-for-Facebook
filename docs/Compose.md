# Jetpack Compose

Below are some findings and demos for jetpack compose during my explorations in this project. As with most implementation, I believe that anything is possible. This holds true with view implementations of the design below, though we will see that compose makes some elements considerably easier.

All links to code snippets are permalinks, but feel free to view changes at HEAD in case there are updates.

## Settings

> [Settings code](https://github.com/AllanWang/Frost-for-Facebook/tree/f5b003298ee91056e86a63c1f50c25285af45c9b/app-compose/src/main/kotlin/com/pitchedapps/frost/compose/settings)

Compose does not have any settings library, but it is actually very easy to do with [material `ListItem`s](https://m3.material.io/components/lists). This would also be easy with views + recyclerviews, though it seems like it is not being planned for MDC. With list items, we simply change the trailing content to provide what we need, be it a switch, checkbox, text, color selector, etc.

<todo add demo>

For most older apps, the standard is to use android preference xmls to build layouts. There are some downsides:
1. They are primarily built to use shared preferences, which we may not use
2. Custom views require some more wrapping to integrate with preferences
3. We don't control the base layouts at all (though we can modify some via themes)
4. Material no longer supports them

By converting to our own full implementation like in the snippet above, we have full control over the layouts, can add custom ones, and have all the benefits of compose to build relations between preferences, or between pages through the nav graph.

## Animations

### Stateless Animations

Compose makes it easy to animate from one state value to another. For instance, if scale is 1.0 by default, but should be 1.5 when pressed, we simply use `animateFloatAsState` and provide the expected value. However, there are cases where we may want to start and stop at the same value. This is pretty straightforward with views (`View.animate()`, `ValueAnimator`, etc), though compose supports this too:

We can look at the [overview graph](https://developer.android.com/jetpack/compose/animation/introduction#overview) and see some more basic building blocks, including `Animation` and `Animatable`. Both of these allow for initial values + velocity. Even if the initial and target values are the same, we can include velocity (or keyframes) to keep an animation going.

You can see an example through our [shake](https://github.com/AllanWang/Frost-for-Facebook/blob/f5b003298ee91056e86a63c1f50c25285af45c9b/app-compose/src/main/kotlin/com/pitchedapps/frost/compose/effects/Shake.kt) effect. `shake()` is called on click, and shakes that happen before the animation ends will smoothly restart the effect, but from the current rotation value using spring animations.

### Drag & Drop

Dragging between two visual elements is done similarly with views and compose:

* Replicate the element being dragged, and optionally hiding the original layout
* Ensure that the drag element can be drawn across the entire draggable region
* Provide reactions to the drag element when it hovers over other elements
* Make space for the dropped element and build the new layout after the transition is complete

With compose, it is extremely easy to listen to global coordinates, and to add/alter visual elements immediately in the next frame. We simply have listeners for all offsets, and a composable that optionally draws content. The same is possible with views, but takes a bit more coordination to actually add and remove the views. For dragging specifically, compose provides helpers including `detectDragGesturesAfterLongPress`, which does most of the work needed. No need for a `SimpleOnGestureListener`.

https://github.com/AllanWang/Frost-for-Facebook/assets/6251823/0cef497c-cbdc-4597-870f-a38ef8d23b35

