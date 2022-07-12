import React from 'react';
import * as x from './stories.js'

const stories = new x.storybook.playground.HelloStories()

export default {
    title: stories.title,
    component: stories.component,
}

export const helloStory = stories.helloStory
export const helloUniverse = stories.helloUniverse