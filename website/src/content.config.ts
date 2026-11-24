import { defineCollection } from 'astro:content';
import { glob } from 'astro/loaders';
import { z } from 'astro/zod';
import { docsLoader } from '@astrojs/starlight/loaders';
import { docsSchema } from '@astrojs/starlight/schema';

export const collections = {
  docs: defineCollection({ loader: docsLoader(), schema: docsSchema() }),
  // Separate from the docs, so posts stay out of llms.txt.
  blog: defineCollection({
    loader: glob({ base: './src/content/blog', pattern: '**/*.{md,mdx}' }),
    schema: z.object({
      title: z.string(),
      description: z.string(),
      date: z.coerce.date(),
      author: z.string().default('Régis Blanc'),
      category: z.enum(['Releases', 'Engineering', 'Games']),
      /** An old post about an API that is gone: shows a notice, and stays out of the site search. */
      archived: z.boolean().default(false),
    }),
  }),
};
