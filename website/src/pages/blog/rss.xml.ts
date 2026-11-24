import rss from '@astrojs/rss';
import type { APIContext } from 'astro';

import { blogPosts } from '../../data/blog';

export async function GET(context: APIContext) {
  const posts = await blogPosts();
  return rss({
    title: 'SGL Blog',
    description: 'Releases, engineering, and games made with the Scala Game Library.',
    site: context.site!,
    items: posts.map(({ id, data }) => ({
      title: data.title,
      description: data.description,
      pubDate: data.date,
      categories: [data.category],
      link: `/blog/${id}/`,
    })),
  });
}
