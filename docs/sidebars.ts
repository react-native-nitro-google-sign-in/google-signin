import type { SidebarsConfig } from '@docusaurus/plugin-content-docs'

const sidebars: SidebarsConfig = {
  guideSidebar: [
    'intro',
    {
      type: 'category',
      label: 'Getting started',
      collapsed: false,
      items: [
        'getting-started/requirements',
        'getting-started/installation',
        'getting-started/quick-start',
      ],
    },
    {
      type: 'category',
      label: 'Platform setup',
      collapsed: false,
      items: [
        {
          type: 'doc',
          id: 'setup/google-cloud',
          label: 'Google Cloud & config files',
        },
        'setup/android',
        'setup/ios',
        'setup/expo',
      ],
    },
    {
      type: 'category',
      label: 'Guides',
      items: [
        'guide/usage',
        'guide/api-reference',
        'guide/google-sign-in-button',
        'guide/troubleshooting',
      ],
    },
    {
      type: 'category',
      label: 'Examples',
      items: ['examples/overview'],
    },
    {
      type: 'category',
      label: 'Community',
      collapsed: true,
      items: [
        'community/overview',
        'community/contributing',
        'community/code-of-conduct',
        'community/security',
        'community/support',
      ],
    },
  ],
  agentsSidebar: [
    'agents/overview',
    'agents/skill',
    'agents/rules',
    'agents/prompts',
  ],
}

export default sidebars
